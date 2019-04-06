package me.olliechick.instagramunfollowers

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import dev.niekirk.com.instagram4android.Instagram4Android
import dev.niekirk.com.instagram4android.requests.InstagramSearchUsernameRequest
import dev.niekirk.com.instagram4android.requests.payload.InstagramSearchUsernameResult
import org.jetbrains.anko.doAsync


class Util {

    companion object {
        var instagram: Instagram4Android? = null
        val prefsFile = "me.olliechick.instagramunfollowers.prefs"
        val helpUrl = "https://docs.google.com/document/d/1-LhlALXtHtUy6Em9Hb6cTdF0Hbwm4q4N6iKVuOJ0kIE/edit?usp=sharing"
        val TAG = "Unfollowers"

        fun login(prefs: SharedPreferences, username: String, password: String): Boolean {

            instagram = Instagram4Android.builder().username(username).password(password).build()
            instagram!!.setup()

            val instagramLoginResult = instagram!!.login()
            doAsync {
                saveCredentials(prefs, username, password)
            }


            return instagramLoginResult.status == "ok"
        }

        fun loginFromSharedPrefs(prefs: SharedPreferences): Boolean {
            val username = prefs.getString("username", null)
            val password = prefs.getString("password", null)

            if (username == null || password == null) {
                return false
            }

            return login(prefs, username, password)
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

        private fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
            // Adapted from https://stackoverflow.com/a/5921190/8355496
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            for (service in manager!!.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }
    }
}