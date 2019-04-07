package me.olliechick.instagramunfollowers


import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.util.Log
import dev.niekirk.com.instagram4android.requests.InstagramGetUserFollowersRequest
import dev.niekirk.com.instagram4android.requests.payload.InstagramUserSummary
import me.olliechick.instagramunfollowers.Util.Companion.initialiseDb
import me.olliechick.instagramunfollowers.Util.Companion.loginFromSharedPrefs
import me.olliechick.instagramunfollowers.Util.Companion.prefsFile
import java.time.OffsetDateTime


class GetFollowersService : IntentService("GetFollowersService") {

    private lateinit var db: AppDatabase
    private var followingId: Long = 0

    // will be called asynchronously by Android
    override fun onHandleIntent(intent: Intent?) {
        followingId = intent!!.getLongExtra("id", 0)
        if (followingId.toInt() == 0) Log.e(Util.TAG, "Id = 0 in GetFollowersService")
        val followerSummaries = getFollowers(followingId)
        val now = OffsetDateTime.now()
        val followers = instagramUserSummaryListToFollowerList(followerSummaries, now)

        db = initialiseDb(applicationContext)
        try {
            saveFollowers(followers)
            updateLastUpdated(followingId, now)
            publishResults(true)
        } catch (e: SQLiteException) {
            Log.e(Util.TAG, e.message)
            publishResults(false, e)
        } finally {
            db.close()
        }
    }

    private fun updateLastUpdated(followingId: Long, now: OffsetDateTime) {
        db.accountDao().updateLastUpdated(followingId, now)
    }

    private fun instagramUserSummaryListToFollowerList(
        followerSummaries: List<InstagramUserSummary>,
        now: OffsetDateTime
    ): List<Follower> = followerSummaries.map { Follower(it.pk, now, it.username, it.full_name, followingId) }


    private fun getFollowers(id: Long): List<InstagramUserSummary> {
        if (Util.instagram == null) {
            val loginSuccess = loginFromSharedPrefs(getSharedPreferences(prefsFile, Context.MODE_PRIVATE))
            if (!loginSuccess) throw IllegalArgumentException("Login failed.")
            if (Util.instagram == null) {
                Log.wtf(Util.TAG, "instagram is still null after logging in from sharedprefs was successful")
                return listOf()
            }
        }

        val users = mutableListOf<InstagramUserSummary>()
        var res = Util.instagram!!.sendRequest(InstagramGetUserFollowersRequest(id))
        users.addAll(res.users)
        while (res.next_max_id != null) {
            res = Util.instagram!!.sendRequest(InstagramGetUserFollowersRequest(id, res.next_max_id))
            users.addAll(res.users)
        }
        return users
    }

    private fun saveFollowers(followers: List<Follower>) {
        val ids = db.accountDao().getIds()
        ids.forEach {
            Log.i(Util.TAG, "$it (type = ${it.javaClass.kotlin})")
        }
        db.followerDao().insertAll(*followers.map { it }.toTypedArray())
    }


    private fun publishResults(saved: Boolean, e: Exception?) {
        val intent = Intent(NOTIFICATION)
        intent.putExtra("saved", saved)
        if (e != null) intent.putExtra("exception", e)
        sendBroadcast(intent)
    }

    private fun publishResults(saved: Boolean) {
        publishResults(saved, null)
    }

    companion object {
        val NOTIFICATION = "me.olliechick.instagramunfollowers.UnfollowersListActivity"
    }
}