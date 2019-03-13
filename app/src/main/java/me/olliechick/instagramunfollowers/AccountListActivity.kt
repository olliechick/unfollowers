package me.olliechick.instagramunfollowers

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import dev.niekirk.com.instagram4android.requests.InstagramGetUserFollowersRequest
import dev.niekirk.com.instagram4android.requests.InstagramSearchUsernameRequest
import me.olliechick.instagramunfollowers.MyApplication.Companion.instagram
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AccountListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_list)
//        setSupportActionBar(findViewById(R.id.toolbar))
        populateList()
    }

    private fun populateList() {
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

        }
    }
}
