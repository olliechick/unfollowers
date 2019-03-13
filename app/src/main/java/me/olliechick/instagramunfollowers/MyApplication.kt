package me.olliechick.instagramunfollowers

import android.app.Application
import dev.niekirk.com.instagram4android.Instagram4Android

class MyApplication : Application() {

    companion object {
        var instagram: Instagram4Android? = null
    }
}