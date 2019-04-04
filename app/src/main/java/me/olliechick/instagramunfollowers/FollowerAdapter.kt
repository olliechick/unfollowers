package me.olliechick.instagramunfollowers

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.time.format.DateTimeFormatter

class FollowerAdapter(
    private val context: Context,
    val followers: ArrayList<Follower>,
    val clickListener: (Follower) -> Unit
) : RecyclerView.Adapter<AccountViewHolder>() {

    override fun getItemCount(): Int = followers.size

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): AccountViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.account_item, parent, false)
        val holder = AccountViewHolder(view)

        holder.itemView.setOnClickListener { clickListener(followers[holder.adapterPosition]) }

        return holder
    }

    override fun onBindViewHolder(holder: AccountViewHolder, i: Int) {
        val name = followers[i].name
        val username = followers[i].username
        val created = followers[i].timestamp

        var displayText  = ""
        if (name.replace("\\s".toRegex(), "") == "") displayText = username
        else displayText = "$name ($username)"
        displayText += " (${created.format(DateTimeFormatter.ofPattern("d MMM"))})"

        holder.accountName.text = displayText
    }
}