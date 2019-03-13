package me.olliechick.instagramunfollowers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.jetbrains.anko.toast

class SplashScreenActivity : Activity() {

    private val MY_PREFS_NAME = "InstagramUnfollowers"

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
        val prefs = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString("username", null)

        if (username == null) {
            // Go to login page
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        } else {
            // Go to account list page
            val intent = Intent(this, AccountListActivity::class.java)
            startActivity(intent)
        }
    }
}
