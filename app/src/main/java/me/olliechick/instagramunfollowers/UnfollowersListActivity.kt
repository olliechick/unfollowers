package me.olliechick.instagramunfollowers

import android.app.Activity
import android.arch.persistence.room.Room
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_unfollowers_list.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast


class UnfollowersListActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var following_username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unfollowers_list)

        following_username = intent.getStringExtra("username")

        val actionBar = supportActionBar
        actionBar?.title = "${getString(R.string.app_name)}: $following_username"

        fab.setOnClickListener { refresh() }

        populateList()
    }

    var unfollowers: ArrayList<Follower> = arrayListOf()
        set(value) {
            field = value
//            accountList.adapter = FollowerAdapter(this, field) {
//                val intent = Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("https://www.instagram.com/${it.username}/")
//                )
//                startActivity(intent)
//            }
        }

    private fun getFollowerDao(): FollowerDao {
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "db"
        ).build()
        return db.followerDao()
    }

    private fun populateList() {
        val layoutManager = LinearLayoutManager(this)
        unfollowerList.layoutManager = layoutManager

        doAsync {
            unfollowers = ArrayList(getFollowerDao().getAllFollowersOfAUser(following_username))
        }

        val decoration = DividerItemDecoration(this, layoutManager.orientation)
        unfollowerList.addItemDecoration(decoration)
    }

    private fun refresh() {

        val intent = Intent(this, GetFollowersService::class.java)
        intent.putExtra("username", following_username)
        startService(intent)
        toast("Refreshing...")
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras
            if (bundle != null) {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(
                        context,
                        bundle.getString("data"),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context, "Download failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
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
