package me.olliechick.instagramunfollowers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

open class AppCompatActivityWithMenu : AppCompatActivity() {


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.mainmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            true
        }

        R.id.action_help -> {
            val uri = Uri.parse(Util.helpUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            true
        }


        R.id.action_log_out -> {
            val prefs = getSharedPreferences(Util.prefsFile, Context.MODE_PRIVATE)
            Util.logout(prefs)

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            true
        }
        else -> super.onOptionsItemSelected(item)

    }


}