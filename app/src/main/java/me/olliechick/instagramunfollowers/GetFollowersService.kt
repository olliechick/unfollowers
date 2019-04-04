package me.olliechick.instagramunfollowers


import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.Room
import dev.niekirk.com.instagram4android.requests.InstagramGetUserFollowersRequest
import dev.niekirk.com.instagram4android.requests.payload.InstagramUserSummary
import java.time.OffsetDateTime


class GetFollowersService : IntentService("DownloadService") {

    private var result = Activity.RESULT_CANCELED
    private lateinit var db: AppDatabase
    private var followingId: Long = 0

    // will be called asynchronously by Android
    override fun onHandleIntent(intent: Intent?) {
        val followingId = intent!!.getLongExtra("id", 0)
        if (followingId.toInt() == 0) Log.e(Util.TAG, "Id = 0 in GetFollowersService")
        val followerSummaries = getFollowers(followingId)
        val followers = instagramUserSummaryListToFollowerList(followerSummaries)
        var saved: Boolean
        try {
            saveFollowers(followers)
            saved = true
        } catch (e: SQLiteConstraintException) {
            saved = false
            Log.e(Util.TAG, e.message)

        }
        initialiseDb()
        publishResults("$saved fid: $followingId " +
                "\n${db.accountDao().getUserFromId(followingId)}")
        db.close()
    }

    private fun instagramUserSummaryListToFollowerList(followerSummaries: List<InstagramUserSummary>): List<Follower> =
        followerSummaries.map {
            Follower(it.pk, OffsetDateTime.now(), it.username, it.full_name, followingId)
        }


    private fun getFollowers(id: Long): List<InstagramUserSummary> =
        if (Util.instagram != null) {
            Util.instagram!!.sendRequest(InstagramGetUserFollowersRequest(id)).users
        } else {
            Log.wtf(Util.TAG, "Trying to get followers but instagram = null")
            listOf()
        }

    private fun initialiseDb() {
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "db"
        ).build()
    }

    private fun saveFollowers(followers: List<Follower>) {
        initialiseDb()
        val ids = db.accountDao().getIds()
        Log.i(Util.TAG, "Id from follower 0: ${followers[0].id} (type = ${followers[0].id.javaClass.kotlin})")
        ids.forEach {
            Log.i(Util.TAG, "$it (type = ${it.javaClass.kotlin})")
        }
        db.followerDao().insertAll(*followers.map { it }.toTypedArray())
        db.close()
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