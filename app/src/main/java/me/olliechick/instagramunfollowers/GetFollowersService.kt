package me.olliechick.instagramunfollowers


import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.Room
import dev.niekirk.com.instagram4android.requests.InstagramGetUserFollowersRequest
import dev.niekirk.com.instagram4android.requests.payload.InstagramUserSummary
import me.olliechick.instagramunfollowers.Util.Companion.initialiseDb
import java.time.OffsetDateTime


class GetFollowersService : IntentService("DownloadService") {

    private var result = Activity.RESULT_CANCELED
    private lateinit var db: AppDatabase
    private var followingId: Long = 0

    // will be called asynchronously by Android
    override fun onHandleIntent(intent: Intent?) {
        followingId = intent!!.getLongExtra("id", 0)
        if (followingId.toInt() == 0) Log.e(Util.TAG, "Id = 0 in GetFollowersService")
        val followerSummaries = getFollowers(followingId)
        val now = OffsetDateTime.now()
        val followers = instagramUserSummaryListToFollowerList(followerSummaries, now)
        var saved: Boolean

        db = initialiseDb(applicationContext)
        try {
            saveFollowers(followers)
            updateLastUpdated(followingId, now)
            saved = true
        } catch (e: SQLiteConstraintException) {
            saved = false
            Log.e(Util.TAG, e.message)
        } finally {
            db.close()
        }

        publishResults(if (saved) "done :)" else "sql error")
    }

    private fun updateLastUpdated(followingId: Long, now: OffsetDateTime) {
        db.accountDao().updateLastUpdated(followingId, now)
    }

    private fun instagramUserSummaryListToFollowerList(
        followerSummaries: List<InstagramUserSummary>,
        now: OffsetDateTime
    ): List<Follower> = followerSummaries.map { Follower(it.pk, now, it.username, it.full_name, followingId) }


    private fun getFollowers(id: Long): List<InstagramUserSummary> =
        if (Util.instagram != null) {
            Util.instagram!!.sendRequest(InstagramGetUserFollowersRequest(id)).users
        } else {
            Log.wtf(Util.TAG, "Trying to get followers but instagram = null")
            listOf()
        }

    private fun saveFollowers(followers: List<Follower>) {
        val ids = db.accountDao().getIds()
        ids.forEach {
            Log.i(Util.TAG, "$it (type = ${it.javaClass.kotlin})")
        }
        db.followerDao().insertAll(*followers.map { it }.toTypedArray())
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