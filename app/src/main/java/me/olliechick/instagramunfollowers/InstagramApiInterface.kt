package me.olliechick.instagramunfollowers

import android.net.Uri

val CLIENT_ID = "61371bd49fc440efacbeac38611acb09"
val REDIRECT_URI = "http://olliechick.me"
val AUTH_URI = Uri.parse("https://api.instagram.com/oauth/authorize/" +
        "?client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URI&response_type=token")