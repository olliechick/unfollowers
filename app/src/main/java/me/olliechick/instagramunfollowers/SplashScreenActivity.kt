package me.olliechick.instagramunfollowers

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import me.olliechick.instagramunfollowers.Util.Companion.TAG
import me.olliechick.instagramunfollowers.Util.Companion.loginFromSharedPrefs
import me.olliechick.instagramunfollowers.Util.Companion.prefsFile
import me.olliechick.instagramunfollowers.Util.Companion.setDefaults
import me.olliechick.instagramunfollowers.Util.Companion.showInternetConnectivityErrorDialog
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.IOException

class SplashScreenActivity : Activity() {
    private val getFollowersReceiver = GetFollowersReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
//        val intent = Intent(applicationContext, AlarmReceiver::class.java).let {
//            PendingIntent.getBroadcast(applicationContext, 0, it, 0)
//        }
        registerReceiver(
            getFollowersReceiver, IntentFilter(
                GetFollowersService.NOTIFICATION
            )
        )
        setDefaults(this)
        createNotificationChannel()
        routeToAppropriatePage()
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(getFollowersReceiver)
        } catch (e: IllegalArgumentException) {
            if (Debug.LOG) Log.i(TAG, "getFollowersReceiver tried to be unregistered without first being registered.")
        }
    }

    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(Notification.CATEGORY_SOCIAL, getString(R.string.new_unfollowers), importance)
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun routeToAppropriatePage() {
        val prefs = getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        val username = prefs.getString("username", null)

        if (username == null) {
            goToLoginPage()
        } else {
            val context = this
            toast(getString(R.string.logging_in_to, username))
            doAsync {
                try {
                    val loginSuccess = loginFromSharedPrefs(prefs)
                    uiThread {
                        if (loginSuccess) {
                            val intent = Intent(context, AccountListActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            toast(getString(R.string.username_and_password_broken, username))
                            goToLoginPage()
                        }
                    }
                } catch (e: IOException) {
                    if (Debug.LOG) Log.i(Util.TAG, "${e.message}")
                    uiThread {
                        showInternetConnectivityErrorDialog(
                            context,
                            ::routeToAppropriatePage,
                            context::finishAndRemoveTask
                        )
                    }
                }
            }
        }

    }

    private fun goToLoginPage() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
