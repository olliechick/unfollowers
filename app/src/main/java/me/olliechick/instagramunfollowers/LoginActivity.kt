package me.olliechick.instagramunfollowers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import dev.niekirk.com.instagram4android.Instagram4Android
import kotlinx.android.synthetic.main.activity_login.*
import me.olliechick.instagramunfollowers.MyApplication.Companion.instagram
import me.olliechick.instagramunfollowers.MyApplication.Companion.prefsFile
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread


/**
 * A login screen that offers login via username/password.
 */
class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        log_in_button.setOnClickListener { login() }
    }

    /** Called when the user taps the Log in button */
    private fun login() {
        val username_text = username.text.toString()
        val password_text = password.text.toString()
        if (username_text.isEmpty()) emptyFieldError(username)
        else if (password_text.isEmpty()) emptyFieldError(password)
        else {
            toast("Logging in...")
            //todo block UI to avoid user pressing button more than once (see how default login activity does it)

            instagram = Instagram4Android.builder()
                .username(username_text).password(password_text).build()
            instagram!!.setup()

            doAsync {
                val instagramLoginResult = instagram!!.login()
                val loginSuccess = instagramLoginResult.status == "ok"

                uiThread {
                    if (loginSuccess) {
                        saveCredentials(username_text, password_text)
                        openAccountList()
                    }
                    else wrongPassword()
                }

            }
        }
    }

    /**
     * Saves the username and password to the shared preferences.
     * This means the user doesn't have to log in each time they start the app.
     */
    private fun saveCredentials(username: String, password: String) {
        val prefs = getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        val prefsEditor = prefs.edit()
        prefsEditor.putString("username", username)
        prefsEditor.putString("password", password)
        prefsEditor.apply()
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
