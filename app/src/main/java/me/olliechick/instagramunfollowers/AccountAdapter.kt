package me.olliechick.instagramunfollowers

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.time.format.DateTimeFormatter

class AccountAdapter(
    private val context: Context,
    val accounts: ArrayList<Account>,
    val clickListener: (Account) -> Unit
) : RecyclerView.Adapter<AccountViewHolder>() {

    private var selectedIndex = RecyclerView.NO_POSITION

    override fun getItemCount(): Int = accounts.size

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): AccountViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.account_item, parent, false)
        val holder = AccountViewHolder(view)

        holder.itemView.setOnClickListener {
            val oldSelectedIndex = selectedIndex
            selectedIndex = holder.adapterPosition
            clickListener(accounts[holder.adapterPosition])
            notifyItemChanged(oldSelectedIndex)
            notifyItemChanged(selectedIndex)
        }

        return holder
    }

    override fun onBindViewHolder(holder: AccountViewHolder, i: Int) {
        val name = accounts[i].name
        val username = accounts[i].username
        val created = accounts[i].created

        var displayText  = ""
        if (name.replace("\\s".toRegex(), "") == "") displayText = username
        else displayText = "$name ($username)"
        displayText += " (${created.format(DateTimeFormatter.ofPattern("d MMM"))})"

        holder.accountName.text = displayText
    }
}

class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val accountName: TextView = view.findViewById(R.id.accountText)
}
