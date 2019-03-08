package me.olliechick.instagramunfollowers

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }

    /** Called when the user taps the Log in button */
    fun logIn(view: View) {
        // Do something in response to button
        Toast.makeText(this, "You clicked log in!", Toast.LENGTH_LONG).show()
    }
}