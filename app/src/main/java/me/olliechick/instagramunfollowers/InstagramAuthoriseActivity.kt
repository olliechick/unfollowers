package me.olliechick.instagramunfollowers

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import dev.niekirk.com.instagram4android.Instagram4Android
import dev.niekirk.com.instagram4android.requests.InstagramGetUserFollowersRequest
import dev.niekirk.com.instagram4android.requests.InstagramSearchUsernameRequest
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread


class InstagramAuthoriseActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instagram_authorise)
    }

    /** Called when the user taps the Log in button */
    fun logIn(view: View) {
        // Do something in response to button
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
        val username = findViewById<TextView>(R.id.usernameField).text.toString()
        val password = findViewById<TextView>(R.id.passwordField).text.toString()
        loginToInstagram(username, password)
    }

    fun loginToInstagram(username: String, password: String) {
        val instagram = Instagram4Android.builder().username(username).password(password).build()
        instagram.setup()
        var toastMessage: String
        val context = this
        doAsync {
            //Execute all the lon running tasks here
            val instagramLoginResult = instagram.login()

            if (instagramLoginResult.getStatus().equals("ok")) {
                toastMessage = "Login success"
            } else {
                toastMessage = "Login failed"
            }
            uiThread {
                //Update the UI thread here
                try {
                    toast(toastMessage)
                    if (toastMessage == "Login success") {
                        doAsync {
                            val result = instagram.sendRequest(InstagramSearchUsernameRequest("ollienickchick"))
                            val followingResult =
                                instagram.sendRequest(InstagramGetUserFollowersRequest(result.user.getPk()))
                            uiThread {
                                try {
                                    val user = result.user

//                                    toast(user.follower_count.toString())
                                    toast(followingResult.users[0].full_name.toString() + followingResult.users[1].full_name.toString())
                                    val listView = findViewById<ListView>(R.id.listView)
                                    val prodAdapter = ArrayAdapter<String>(context,
                                        android.R.layout.simple_list_item_1,
                                        followingResult.users.map { "${it.full_name} (${it.username})" })
                                    listView.adapter = prodAdapter
                                } catch (e: Exception) {
                                    toast(e.toString())
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    toast(e.toString())
                }
            }
        }
    }
}