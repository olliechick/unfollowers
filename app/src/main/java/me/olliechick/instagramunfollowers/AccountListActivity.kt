package me.olliechick.instagramunfollowers

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
import android.widget.ArrayAdapter
import me.olliechick.instagramunfollowers.MyApplication.Companion.logout
import org.jetbrains.anko.toast

class AccountListActivity : AppCompatActivity() {
    private lateinit var accountList: RecyclerView

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
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.get_url()))
                startActivity((intent))
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.mainmenu, menu)
        return super.onCreateOptionsMenu(menu)
//        actionBar?.menu
//        actionBar?.inflate
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
        accountList = findViewById<RecyclerView>(R.id.accountList)
        val layoutManager = LinearLayoutManager(this)
        accountList.layoutManager = layoutManager

        accounts = listOf(
            Account("Ollie Chick", "ollienickchick"),
            Account("Instagram", "instagram"),
            Account("adam", "adam"),
            Account("george", "george"),
            Account("shosahna", "shosahna"),
            Account("1"),
            Account("2"),
            Account("3"),
            Account("4"),
            Account("5"),
            Account("6"),
            Account("7"),
            Account("8"),
            Account("9"),
            Account("a"),
            Account("b"),
            Account("c"),
            Account("d"),
            Account("e"),
            Account("f"),
            Account("g"),
            Account("h"),
            Account("i"),
            Account("j")
        )

        val decoration = DividerItemDecoration(this, layoutManager.orientation)
        accountList.addItemDecoration(decoration)
    }
}
