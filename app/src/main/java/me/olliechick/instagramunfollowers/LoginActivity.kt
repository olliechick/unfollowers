package me.olliechick.instagramunfollowers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import dev.niekirk.com.instagram4android.Instagram4Android
import kotlinx.android.synthetic.main.activity_login.*
import me.olliechick.instagramunfollowers.MyApplication.Companion.instagram
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread


/**
 * A login screen that offers login via username/password.
 */
class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        log_in_button.setOnClickListener { login() }
    }

    /** Called when the user taps the Log in button */
    private fun login() {
        if (username.text.isEmpty()) emptyFieldError(username)
        else if (password.text.isEmpty()) emptyFieldError(password)
        else {
            toast("Logging in...")

            instagram =
                Instagram4Android.builder().username(username.text.toString()).password(password.text.toString())
                    .build()
            instagram!!.setup()
            doAsync {
                val instagramLoginResult = instagram!!.login()
                val loginSuccess = instagramLoginResult.status == "ok"

                uiThread {
                    if (loginSuccess) openAccountList()
                    else wrongPassword()
                }
            }
        }
    }

    private fun emptyFieldError(editText: EditText) {
        editText.error = getString(R.string.error_empty_field)
    }

    private fun wrongPassword() {
                password.error = getString(R.string.error_incorrect_password)
                password.requestFocus()
    }

    private fun openAccountList() {
        val intent = Intent(this, AccountListActivity::class.java)
        startActivity(intent)
    }
}
