package me.olliechick.instagramunfollowers

import android.content.Intent
import android.content.res.Resources
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
            intent.data = Uri.parse("mailto:" + Resources.getSystem().getString(R.string.dev_email))
            intent.putExtra(Intent.EXTRA_SUBJECT, Resources.getSystem().getString(R.string.app_name))
            startActivity(intent)
            true
        }
    }

}