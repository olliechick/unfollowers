package me.olliechick.instagramunfollowers

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView

class UnfollowersListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unfollowers_list)

        val textView = findViewById<TextView>(R.id.textView)

        val username = getIntent().getStringExtra("username")

        val actionBar = supportActionBar
        actionBar?.title = "Unfollowers: $username"

        textView.text = "hello!"
    }
}
