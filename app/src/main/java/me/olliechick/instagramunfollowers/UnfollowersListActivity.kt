package me.olliechick.instagramunfollowers

import android.app.Activity
import androidx.room.Room
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_unfollowers_list.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class UnfollowersListActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var followingUsername: String
    private var followingId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unfollowers_list)

        followingUsername = intent.getStringExtra("username")
        followingId = intent.getLongExtra("id", 0)
        if (followingId.toInt() == 0) Log.e(Util.TAG, "Id = 0 in UnfollowersListActivity")

        val actionBar = supportActionBar
        actionBar?.title = "${getString(R.string.app_name)}: $followingUsername"

        fab.setOnClickListener { refresh() }

        populateList()
    }

    var unfollowers: ArrayList<Follower> = arrayListOf()
        set(value) {
            field = value
            Log.i(Util.TAG, "updating unfollowers")
            unfollowerList.adapter = FollowerAdapter(this, field) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.instagram.com/${it.username}/")
                )
                startActivity(intent)
            }
        }

    private fun initialiseDb() {
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "db"
        ).build()
    }

    private fun populateList() {
        val layoutManager = LinearLayoutManager(this)
        unfollowerList.layoutManager = layoutManager
        unfollowers = arrayListOf()

        doAsync {
            initialiseDb()
            val latestFollowers = db.followerDao().getLatestFollowersForEachId(followingId)
            val latestUpdateTime = db.accountDao().getLatestUpdateTime(followingId)
            val unf: MutableList<Follower> = mutableListOf()

            latestFollowers.forEach {
                if (!it.timestamp.isEqual(latestUpdateTime)) {
                    unf.add(it)
                }
            }

            Log.i(Util.TAG, "Follower list")
            val fol = ArrayList(db.followerDao().getAllFollowersOfAUser(followingId))
            fol.forEach {
                Log.i(Util.TAG, "${it.username}, ${it.timestamp.format(DateTimeFormatter.ofPattern("hh:mm:ss d MMM"))}")
            }

            db.close()
            uiThread {
                unfollowers = ArrayList(unf)
            }
        }

        val decoration = DividerItemDecoration(this, layoutManager.orientation)
        unfollowerList.addItemDecoration(decoration)
    }

    private fun refresh() {
        val intent = Intent(this, GetFollowersService::class.java)
        intent.putExtra("username", followingUsername)
        intent.putExtra("id", followingId)
        startService(intent)
        toast("Refreshing...")
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            populateList()
            toast("Done!")
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            receiver, IntentFilter(
                GetFollowersService.NOTIFICATION
            )
        )
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }
}
