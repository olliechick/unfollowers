package me.olliechick.instagramunfollowers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import me.olliechick.instagramunfollowers.Util.Companion.TAG

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Got ${intent.action}.")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "Got boot event.")
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            Util.scheduleGetFollowers(context)
            if (prefs.getBoolean("refresh_on_startup", true)) {
                Util.getFollowersNow(context)
            }
        }
    }
}
