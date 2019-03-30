package me.olliechick.instagramunfollowers

import android.app.Application
import android.content.SharedPreferences
import dev.niekirk.com.instagram4android.Instagram4Android
import org.jetbrains.anko.doAsync

class MyApplication : Application() {

    companion object {
        var instagram: Instagram4Android? = null
        val prefsFile = "me.olliechick.instagramunfollowers.prefs"

        fun login(prefs: SharedPreferences, username: String, password: String): Boolean {

            instagram = Instagram4Android.builder().username(username).password(password).build()
            instagram!!.setup()

            val instagramLoginResult = instagram!!.login()
            doAsync {
                saveCredentials(prefs, username, password)
            }


            return instagramLoginResult.status == "ok"
        }

        fun loginFromSharedPrefs(prefs: SharedPreferences): Boolean {
            val username = prefs.getString("username", null)
            val password = prefs.getString("password", null)

            if (username == null || password == null) {
                return false
            }

            return login(prefs, username, password)
        }

        fun logout(prefs: SharedPreferences) {
            instagram = null
            removeCredentials(prefs)
        }

        /**
         * Saves the username and password to the shared preferences.
         * This means the user doesn't have to log in each time they start the app.
         */
        private fun saveCredentials(prefs: SharedPreferences, username: String, password: String) {
            val prefsEditor = prefs.edit()
            prefsEditor.putString("username", username)
            prefsEditor.putString("password", password)
            prefsEditor.apply()
        }

        /**
         * Deletes the username and password from the shared preferences.
         */
        private fun removeCredentials(prefs: SharedPreferences) {
            val prefsEditor = prefs.edit()
            prefsEditor.remove("username")
            prefsEditor.remove("password")
            prefsEditor.apply()
        }


    }
}