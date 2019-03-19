package me.olliechick.instagramunfollowers

class Account(val name: String, val username: String) {
    constructor(name: String) : this(name, name)

    fun get_url(): String {
        return "https://www.instagram.com/$username/"
    }
}