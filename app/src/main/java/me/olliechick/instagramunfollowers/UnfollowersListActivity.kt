package me.olliechick.instagramunfollowers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_unfollowers_list.*
import me.olliechick.instagramunfollowers.Util.Companion.getCurrentUnfollowers
import me.olliechick.instagramunfollowers.Util.Companion.openInstagramAccountIntent
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread


class UnfollowersListActivity : AppCompatActivityWithMenu() {
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menu?.findItem(R.id.action_delete)?.isVisible = true
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_delete -> {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.confirm_deletion, followingUsername))
            builder.setPositiveButton(R.string.action_delete) { _, _ -> deleteItem() }
            builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            builder.show()

            unfollowerList.adapter?.notifyDataSetChanged() //todo just notify there was one deleted
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun deleteItem() {
        val context = this
        doAsync {
            val db = Util.initialiseDb(applicationContext)
            db.accountDao().delete(followingUsername)
            db.close()
            uiThread {
                val intent = Intent(context, AccountListActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
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

    override fun onStop() {
        super.onStop()
        fab.clearAnimation()
    }

    private fun populateList() {
        unfollowerList.visibility = View.GONE
        empty_view.visibility = View.VISIBLE
        empty_view.text = getString(R.string.loading)
        val layoutManager = LinearLayoutManager(this)
        unfollowerList.layoutManager = layoutManager
        unfollowers = arrayListOf()

        doAsync {
            val unf = getCurrentUnfollowers(applicationContext, followingId)
            uiThread {
                unfollowers = ArrayList(unf)
                if (unfollowers.size == 0) {
                    empty_view.text = getString(R.string.no_unfollowers)
                } else {
                    unfollowerList.visibility = View.VISIBLE
                    empty_view.visibility = View.GONE

                }
            }
        }

        val decoration = DividerItemDecoration(this, layoutManager.orientation)
        unfollowerList.addItemDecoration(decoration)
    }

    private fun refresh() {
        if (isRefreshing) toast(getString(R.string.refresh_already))
        else {
            isRefreshing = true
            val intent = Intent(this, GetFollowersService::class.java)
            intent.putExtra("id", followingId)
            startService(intent)
            toast(getString(R.string.refreshing))
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
                startActivity(openInstagramAccountIntent(it.username))
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
                toast(getString(R.string.done))
            } else {
                showErrorDialog(getString(R.string.db_error, e, e?.stackTrace))
            }
            fab.clearAnimation()
            isRefreshing = false
        }
    }
}
