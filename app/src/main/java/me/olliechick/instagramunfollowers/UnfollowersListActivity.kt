package me.olliechick.instagramunfollowers

import android.arch.persistence.room.Room
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_account_list.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast

class UnfollowersListActivity : AppCompatActivity() {
    private lateinit var unfollowerList: RecyclerView
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unfollowers_list)

        val username = intent.getStringExtra("username")

        val actionBar = supportActionBar
        actionBar?.title = "${getString(R.string.app_name)}: $username"

        fab.setOnClickListener { refresh() }

        populateList()
    }

    var unfollowers: ArrayList<Account> = arrayListOf()
        set(value) {
            field = value
            accountList.adapter = AccountAdapter(this, field) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.instagram.com/${it.username}/")
                )
                startActivity(intent)
            }
        }

    private fun getUnfollowerDao(): AccountDao{
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "db"
        ).build()
        return db.accountDao()
    }

    private fun populateList() {
        unfollowerList = findViewById(R.id.accountList)
        val layoutManager = LinearLayoutManager(this)
        accountList.layoutManager = layoutManager

        doAsync {
            unfollowers = ArrayList(getUnfollowerDao().getAll())
        }

        val decoration = DividerItemDecoration(this, layoutManager.orientation)
        accountList.addItemDecoration(decoration)
    }

    private fun refresh() {
        toast("Refreshing not yet implemented.")
    }
}
