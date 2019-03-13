package me.olliechick.instagramunfollowers

import android.app.Activity
import android.os.Bundle

class AccountListActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_list)
    }
}
