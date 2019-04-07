package me.olliechick.instagramunfollowers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import me.olliechick.instagramunfollowers.Util.Companion.loginFromSharedPrefs
import me.olliechick.instagramunfollowers.Util.Companion.prefsFile
import me.olliechick.instagramunfollowers.Util.Companion.showInternetConnectivityErrorDialog
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.IOException

class SplashScreenActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        routeToAppropriatePage()
    }

    private fun routeToAppropriatePage() {
        val prefs = getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        val username = prefs.getString("username", null)

        if (username == null) {
            goToLoginPage()
        } else {
            val context = this
            toast("Logging in to $username...")
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
                            toast("Your username and password for $username no longer work :(")
                            goToLoginPage()
                        }
                    }
                } catch (e: IOException) {
                    Log.i(Util.TAG, "${e.message}")
                    uiThread {
                        showInternetConnectivityErrorDialog(context, ::routeToAppropriatePage, context::finishAndRemoveTask)
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
