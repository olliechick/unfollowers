package me.olliechick.instagramunfollowers

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.time.format.DateTimeFormatter

class AccountAdapter(
    private val context: Context,
    val accounts: ArrayList<Account>,
    val clickListener: (Account) -> Unit
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    override fun getItemCount(): Int = accounts.size

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): AccountViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.account_item, parent, false)
        val holder = AccountViewHolder(view)

        holder.itemView.setOnClickListener { clickListener(accounts[holder.adapterPosition]) }

        return holder
    }

    override fun onBindViewHolder(holder: AccountViewHolder, i: Int) {
        val name = accounts[i].name
        val username = accounts[i].username

        var displayText = ""
        displayText = if (name.replace("\\s".toRegex(), "") == "") username
        else context.getString(R.string.account_format, name, username)

        holder.accountName.text = displayText
    }

    class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val accountName: TextView = view.findViewById(R.id.accountText)
    }
}
