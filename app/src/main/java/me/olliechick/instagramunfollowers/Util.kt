package me.olliechick.instagramunfollowers

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.room.Room
import dev.niekirk.com.instagram4android.Instagram4Android
import dev.niekirk.com.instagram4android.requests.InstagramSearchUsernameRequest
import dev.niekirk.com.instagram4android.requests.payload.InstagramSearchUsernameResult
import org.jetbrains.anko.doAsync
import java.util.*


class Util {

    companion object {
        var instagram: Instagram4Android? = null
        val prefsFile = "me.olliechick.instagramunfollowers.prefs"
        val helpUrl = "https://docs.google.com/document/d/1-LhlALXtHtUy6Em9Hb6cTdF0Hbwm4q4N6iKVuOJ0kIE/edit?usp=sharing"
        val TAG = "Unfollowers"


        fun login(prefs: SharedPreferences, username: String, password: String, save: Boolean): Boolean {

            instagram = Instagram4Android.builder().username(username).password(password).build()
            instagram!!.setup()

            val instagramLoginResult = instagram!!.login()
            val loginSuccess = instagramLoginResult.status == "ok"
            if (loginSuccess && save) {
                doAsync {
                    saveCredentials(prefs, username, password)
                }
            }
            return loginSuccess
        }

        fun loginFromSharedPrefs(prefs: SharedPreferences): Boolean {
            val username = prefs.getString("username", null)
            val password = prefs.getString("password", null)

            if (username == null || password == null) {
                return false
            }

            return login(prefs, username, password, false)
        }

        fun logout(prefs: SharedPreferences) {
            instagram = null
            removeCredentials(prefs)
        }

        /**
         * Saves the username and password to the shared preferences.
         * This means the user doesn't have to log in each time they start the app.
         */
        private fun saveCredentials(prefs: SharedPreferences, username: String, password: String) {
            val prefsEditor = prefs.edit()
            prefsEditor.putString("username", username)
            prefsEditor.putString("password", password)
            prefsEditor.apply()
        }

        /**
         * Deletes the username and password from the shared preferences.
         */
        private fun removeCredentials(prefs: SharedPreferences) {
            val prefsEditor = prefs.edit()
            prefsEditor.remove("username")
            prefsEditor.remove("password")
            prefsEditor.apply()
        }


        fun getAccount(username: String): InstagramSearchUsernameResult {
            val result = instagram!!.sendRequest(InstagramSearchUsernameRequest(username))
            return result
        }

        fun initialiseDb(applicationContext: Context): AppDatabase {
            return Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "db"
            ).build()
        }

        fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
            // Adapted from https://stackoverflow.com/a/5921190/8355496
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            for (service in manager!!.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        private fun generateEmailErrorDetailsIntent(details: String): Intent {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:" + Resources.getSystem().getString(R.string.dev_email))
            intent.putExtra(
                Intent.EXTRA_SUBJECT,
                Resources.getSystem().getString(R.string.email_subject)
            )
            intent.putExtra(Intent.EXTRA_TEXT, details)
            return intent
        }

        fun showErrorDialog(details: String, context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.an_error_occurred))
            builder.setMessage(context.getString(R.string.send_to_devs))
            builder.setPositiveButton(context.getString(R.string.send)) { _, _ ->
                val emailIntent = generateEmailErrorDetailsIntent(
                    details
                )
                context.startActivity(emailIntent)
            }
            builder.setNegativeButton("Don't send") { dialog, _ -> dialog.cancel() }
            builder.show()
        }

        fun showInternetConnectivityErrorDialog(context: Context, retry: () -> Unit) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.internet_issue))
            builder.setMessage(context.getString(R.string.please_connect))
            builder.setPositiveButton(context.getString(R.string.retry)) { _, _ -> retry() }
            builder.setNegativeButton(context.getString(R.string.dismiss)) { dialog, _ -> dialog.cancel() }
            builder.show()
        }

        fun showInternetConnectivityErrorDialog(context: Context, retry: () -> Unit, closeApp: () -> Unit) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.internet_issue))
            builder.setMessage(context.getString(R.string.please_connect))
            builder.setPositiveButton(R.string.retry) { _, _ -> retry() }
            builder.setNegativeButton(context.getString(R.string.close_app)) { _, _ -> closeApp() }
            builder.show()
        }

        fun scheduleGetFollowers(context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)

            if (prefs.getBoolean("auto_refresh", true)) {
                val today = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, 6)
                    set(Calendar.MINUTE, 0)
                }

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val intervalMillis = minutesToAlarmInterval(prefs.getString("refresh_rate", ""))
                Log.i(TAG, "Notifs set to every $intervalMillis millis.")

                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    today.timeInMillis,
                    intervalMillis,
                    pendingGetFollowersIntent(context)
                )
            }
        }

        fun getFollowersNow(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 100,
                pendingGetFollowersIntent(context)
            )

        }

        private fun pendingGetFollowersIntent(context: Context): PendingIntent {
            return Intent(context, GetFollowersReceiver::class.java).let {
                PendingIntent.getBroadcast(context, 0, it, 0)
            }
        }

        fun setDefaults(context: Context) {
            doAsync {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val refreshRate = sharedPreferences.getString("refresh_rate", "")
                if (refreshRate == "") {
                    // This must be the first time using the app
                    val prefsEditor = sharedPreferences.edit()
                    prefsEditor.putString("refresh_rate", "720")
                    prefsEditor.putBoolean("auto_refresh", true)
                    prefsEditor.putBoolean("refresh_on_startup", true)
                    prefsEditor.apply()
                }
            }
        }

        fun minutesToAlarmInterval(minutes: String?) = when (minutes) {
            "30" -> AlarmManager.INTERVAL_HALF_HOUR
            "60" -> AlarmManager.INTERVAL_HOUR
            "720" -> AlarmManager.INTERVAL_HALF_DAY
            "1440" -> AlarmManager.INTERVAL_DAY
            else -> AlarmManager.INTERVAL_HALF_DAY
        }

        fun getCurrentUnfollowers(context: Context, followingId: Long): List<Follower> {
            val db = initialiseDb(context)
            val latestFollowers = db.followerDao().getLatestFollowersForEachId(followingId)
            val latestUpdateTime = db.accountDao().getLatestUpdateTime(followingId)
            db.close()

            val unfollowers: MutableList<Follower> = mutableListOf()
            latestFollowers.forEach {
                if (!it.timestamp.isEqual(latestUpdateTime)) unfollowers.add(it)
            }

            return unfollowers
        }

        fun openInstagramAccountIntent(username: String) = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.instagram.com/$username/")
        )

        fun usernameIsValid(username: String): Boolean =
            username.length <= 30 && username.matches("[a-z0-9._]+".toRegex())
    }
}