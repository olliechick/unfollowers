package me.olliechick.instagramunfollowers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import me.olliechick.instagramunfollowers.MyApplication.Companion.login
import me.olliechick.instagramunfollowers.MyApplication.Companion.loginFromSharedPrefs
import me.olliechick.instagramunfollowers.MyApplication.Companion.prefsFile
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

class SplashScreenActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val totalMillis = 500
        val timestampStart = System.currentTimeMillis()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        routeToAppropriatePage()

        val elapsedMillis = System.currentTimeMillis() - timestampStart
        val remainingMillis = totalMillis - elapsedMillis

        Thread.sleep(remainingMillis, 0)

    }

    private fun routeToAppropriatePage() {
        val prefs = getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        val username = prefs.getString("username", null)

        if (username == null) goToLoginPage()
        else {
            //todo figure out why we never get here
            val context = this
            toast("Logging in...")
            doAsync {
                val loginSuccess = loginFromSharedPrefs(prefs)
                uiThread {
                    if (loginSuccess) {
                        val intent = Intent(context, AccountListActivity::class.java)
                        startActivity(intent)
                    } else {
                        toast("Your username and password no longer work :(")
                        goToLoginPage()
                    }
                }
            }
        }

    }

    private fun goToLoginPage() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
