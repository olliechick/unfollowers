package me.olliechick.instagramunfollowers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import me.olliechick.instagramunfollowers.MyApplication.Companion.logout
import org.jetbrains.anko.toast

class AccountListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_list)
        populateList()

        val actionBar = supportActionBar

        actionBar?.setTitle("Accounts")

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
        val accountList = arrayListOf("item 1", "item 2", "item 3")
        val prodAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accountList)
        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = prodAdapter

//        val accounts = arrayListOf<String>()
//        try {
//            val file = openFileInput("accounts.json")
//            accounts = JSON.parse()
//        } catch (e: FileNotFoundException) {
//
//        }


        /*
        val context = this
        doAsync {
            if (instagram != null) {
                // test api by getting my followers
                val result = instagram!!.sendRequest(InstagramSearchUsernameRequest("ollienickchick"))
                val followingResult = instagram!!.sendRequest(InstagramGetUserFollowersRequest(result.user.getPk()))
                val listView = findViewById<ListView>(R.id.listView)
                uiThread {
                    val prodAdapter = ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1,
                        followingResult.users.map { "${it.full_name} (${it.username})" })
                    listView.adapter = prodAdapter
                }

            }

        }*/
    }
}
