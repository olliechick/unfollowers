package me.olliechick.instagramunfollowers

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
        setContentView(R.layout.activity_settings)
        val actionBar = supportActionBar
        actionBar?.title = "test title"
    }
}
