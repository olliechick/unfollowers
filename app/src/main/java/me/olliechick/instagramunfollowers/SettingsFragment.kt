package me.olliechick.instagramunfollowers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        initSettings()
    }

    private fun initSettings() {
        findPreference("feedback").setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:olliechick@gmail.com");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Unfollowers app")
            startActivity(intent)
            true
        }
    }

}