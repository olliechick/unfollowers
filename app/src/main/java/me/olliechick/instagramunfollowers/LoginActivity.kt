package me.olliechick.instagramunfollowers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_login.*
import me.olliechick.instagramunfollowers.Util.Companion.TAG
import me.olliechick.instagramunfollowers.Util.Companion.showInternetConnectivityErrorDialog
import me.olliechick.instagramunfollowers.Util.Companion.usernameIsValid
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.IOException
import me.olliechick.instagramunfollowers.Util.Companion.login as login_backend
import android.text.method.PasswordTransformationMethod
import android.graphics.Typeface




/**
 * A login screen that offers login via username/password.
 */
class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        fill_fields_from_shared_prefs()
        log_in_button.setOnClickListener { login() }
    }

    private fun fill_fields_from_shared_prefs() {
        val prefs = getSharedPreferences(Util.prefsFile, Context.MODE_PRIVATE)
        val prefsUsername = prefs.getString("username", "")
        val prefsPassword = prefs.getString("password", "")

        if (prefsUsername != null) username.setText(prefsUsername)
        if (prefsPassword != null) password.setText(prefsPassword)
    }

    /** Called when the user taps the Log in button */
    private fun login() {
        val username_text = username.text.toString()
        val password_text = password.text.toString()
        val prefs = getSharedPreferences(Util.prefsFile, Context.MODE_PRIVATE)
        if (username_text.isEmpty()) emptyFieldError(username)
        else if (password_text.isEmpty()) emptyFieldError(password)
        else if (!usernameIsValid(username_text)) invalidUsernameError()
        else {
            toast(getString(R.string.logging_in))
            //todo block UI to avoid user pressing button more than once (see how default login activity does it)

            val context = this
            doAsync {
                try {
                    val loginSuccess = login_backend(prefs, username_text, password_text, true)

                    uiThread {
                        if (loginSuccess) openAccountList()
                        else wrongPassword()
                    }

                } catch (e: IOException) {
                    Log.i(TAG, "${e.message}")
                    uiThread {
                        showInternetConnectivityErrorDialog(context, ::login)
                    }
                }


            }
        }
    }

    private fun invalidUsernameError() {
        username.error = getString(R.string.username_invalid, username.text)
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
