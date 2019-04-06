package me.olliechick.instagramunfollowers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_unfollowers_list.*
import me.olliechick.instagramunfollowers.Util.Companion.initialiseDb
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread


class UnfollowersListActivity : AppCompatActivityWithMenu() {
    private lateinit var db: AppDatabase
    private lateinit var followingUsername: String
    private var followingId: Long = 0
    private var isRefreshing: Boolean = false

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

    override fun onStop() {
        super.onStop()
        fab.clearAnimation()
    }

    private fun populateList() {
        val layoutManager = LinearLayoutManager(this)
        unfollowerList.layoutManager = layoutManager
        unfollowers = arrayListOf()

        doAsync {
            db = initialiseDb(applicationContext)
            val latestFollowers = db.followerDao().getLatestFollowersForEachId(followingId)
            val latestUpdateTime = db.accountDao().getLatestUpdateTime(followingId)
            db.close()

            val unf: MutableList<Follower> = mutableListOf()
            latestFollowers.forEach {
                if (!it.timestamp.isEqual(latestUpdateTime)) unf.add(it)
            }

            uiThread {
                unfollowers = ArrayList(unf)
            }
        }

        val decoration = DividerItemDecoration(this, layoutManager.orientation)
        unfollowerList.addItemDecoration(decoration)
    }

    private fun refresh() {
        if (isRefreshing) toast("Refresh already in progress...")
        else {
            isRefreshing = true
            val intent = Intent(this, GetFollowersService::class.java)
            intent.putExtra("username", followingUsername)
            intent.putExtra("id", followingId)
            startService(intent)
            toast("Refreshing...")
            spinRefreshFab()
        }
    }

    private fun spinRefreshFab() {
        fab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_indefinitely))
    }

    private var unfollowers: ArrayList<Follower> = arrayListOf()
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

    private fun showErrorDialog(details: String) {
        Util.showErrorDialog(details, this)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val saved = intent.getBooleanExtra("saved", false)
            val e = intent.getSerializableExtra("exception") as Exception?
            if (saved) {
                populateList()
                toast("Done!")
            } else {
                showErrorDialog(
                    "There was an error with the database when attempting to save a new set of followers." +
                            "\n\nError details:\n\n$e\n\n${e?.stackTrace}"
                )
            }
            fab.clearAnimation()
            isRefreshing = false
        }
    }

}
