package me.olliechick.instagramunfollowers

import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import me.olliechick.instagramunfollowers.MyApplication.Companion.logout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast

class AccountListActivity : AppCompatActivity() {
    private lateinit var accountList: RecyclerView
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_list)

        val actionBar = supportActionBar
        actionBar?.title = "Accounts"

        populateList()
    }

    var accounts: List<Account> = listOf()
        set(value) {
            field = value
            accountList.adapter = AccountAdapter(this, field) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/${it.username}/")) //todo change to open unfollower list
                startActivity((intent))
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.mainmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            toast("Settings page not yet implemented")
//          val intent = Intent(this, Settings::class.java)
//          startActivity(intent)
            true
        }

        R.id.action_log_out -> {
            val prefs = getSharedPreferences(MyApplication.prefsFile, Context.MODE_PRIVATE)
            logout(prefs)

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun populateList() {
        accountList = findViewById(R.id.accountList)
        val layoutManager = LinearLayoutManager(this)
        accountList.layoutManager = layoutManager

        doAsync {

            db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "db"
            ).build()
            val dao = db.accountDao()

            //add test data
            dao.insertAll(
                Account(1, "Ollie Chick", "ollienickchick"),
                Account(2, "Instagram", "instagram"),
                Account(3, "adam", "adam"),
                Account(4, "george", "george"),
                Account(5, "shosahna", "shosahna")
            )

            accounts = db.accountDao().getAll()
        }

        val decoration = DividerItemDecoration(this, layoutManager.orientation)
        accountList.addItemDecoration(decoration)
    }
}
