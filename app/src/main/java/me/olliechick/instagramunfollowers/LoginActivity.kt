package me.olliechick.instagramunfollowers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import me.olliechick.instagramunfollowers.MyApplication.Companion.login as login_backend


/**
 * A login screen that offers login via username/password.
 */
class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //todo autofill from shared prefs if they aren't null

        log_in_button.setOnClickListener { login() }
    }

    /** Called when the user taps the Log in button */
    private fun login() {
        val username_text = username.text.toString()
        val password_text = password.text.toString()
        val prefs = getSharedPreferences(MyApplication.prefsFile, Context.MODE_PRIVATE)
        if (username_text.isEmpty()) emptyFieldError(username)
        else if (password_text.isEmpty()) emptyFieldError(password)
        else {
            toast("Logging in...")
            //todo block UI to avoid user pressing button more than once (see how default login activity does it)

            doAsync {
                val loginSuccess = login_backend(prefs, username_text, password_text)

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
