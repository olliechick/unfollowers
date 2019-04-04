package me.olliechick.instagramunfollowers


import android.app.Activity
import android.app.IntentService
import android.content.Intent


class GetFollowersService : IntentService("DownloadService") {

    private var result = Activity.RESULT_CANCELED

    // will be called asynchronously by Android
    override fun onHandleIntent(intent: Intent?) {
        val username = intent!!.getStringExtra("username")
        publishResults("This is only a test $username")
    }

    private fun publishResults(data: String) {
        val intent = Intent(NOTIFICATION)
        intent.putExtra("data", data)
        sendBroadcast(intent)
    }

    companion object {
        val NOTIFICATION = "me.olliechick.instagramunfollowers.UnfollowersListActivity"
    }
}