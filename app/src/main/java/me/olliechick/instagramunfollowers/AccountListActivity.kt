package me.olliechick.instagramunfollowers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_account_list.*
import me.olliechick.instagramunfollowers.Util.Companion.TAG
import me.olliechick.instagramunfollowers.Util.Companion.getAccount
import me.olliechick.instagramunfollowers.Util.Companion.initialiseDb
import me.olliechick.instagramunfollowers.Util.Companion.usernameIsValid
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.time.OffsetDateTime


class AccountListActivity : AppCompatActivityWithMenu(), AddAccountDialogFragment.AddAccountDialogListener {
    private lateinit var db: AppDatabase
    private var allAccounts: List<Account> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_list)

        val actionBar = supportActionBar
        actionBar?.title = getString(R.string.accounts)

        fab.setOnClickListener { openAddAccountDialog() }

        populateList()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menu?.findItem(R.id.action_delete)?.isVisible = true
        return true
    }

    var accounts: ArrayList<Account> = arrayListOf()
        set(value) {
            field = value
            accountList.adapter = AccountAdapter(this, field) {
                val intent = Intent(this, UnfollowersListActivity::class.java)
                intent.putExtra("username", it.username)
                intent.putExtra("id", it.id)
                startActivity(intent)
            }
        }

    private fun populateList() {
        val layoutManager = LinearLayoutManager(this)
        accountList.layoutManager = layoutManager
        accounts = arrayListOf()

        doAsync {
            db = initialiseDb(applicationContext)
            allAccounts = db.accountDao().getAll()
            uiThread {
                accounts = ArrayList(allAccounts)
                if (accounts.size == 0) {
                    accountList.visibility = View.GONE
                    empty_view.visibility = View.VISIBLE
                }
            }
            db.close()
        }

        val decoration = DividerItemDecoration(this, layoutManager.orientation)
        accountList.addItemDecoration(decoration)
    }

    private fun openAddAccountDialog() {
        AddAccountDialogFragment().show(supportFragmentManager, "addAccount")
    }

    override fun onAccountAdded(username: String) {
        // username had already been made lowercase
        if (!usernameIsValid(username)) toast(getString(R.string.username_invalid, username))
        if (accountAlreadyAdded(username)) toast(getString(R.string.username_already_added, username))
        else {
            val context = this
            doAsync {
                val result = getAccount(username)
                if (result.status.toLowerCase() == "ok") {
                    val user = result.user
                    val name = user.full_name
                    val id = user.pk
                    val created = OffsetDateTime.now()
                    val newAccount = Account(id, username, name, created, created)

                    uiThread {
                        Log.i(TAG, "${newAccount.username} has ${user.follower_count} followers.")
                        showAddAccountConfirmationDialog(newAccount, user.follower_count, context)
                    }
                } else {
                    uiThread {
                        Toast.makeText(context, getString(R.string.error_getting_account, username), Toast.LENGTH_SHORT)
                            .show()
                    }

                }

            }
        }
    }

    private fun accountAlreadyAdded(username: String): Boolean {
        allAccounts.forEach { if (it.username == username) return true }
        return false
    }

    private fun showAddAccountConfirmationDialog(account: Account, followerCount: Int, context: Context) {
        val builder = AlertDialog.Builder(context)

        var message = context.getString(R.string.are_you_sure_add, account.name, account.username)
        if (followerCount > 1000)
            message += " " + context.getString(
                R.string.large_follower_warning,
                getFormattedNumber(followerCount, context),
                getTimeString(followerCount, context)
            )
        builder.setMessage(message)

        builder.setPositiveButton(context.getString(R.string.add)) { _, _ -> addAccount(account, context) }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun getFormattedNumber(followerCount: Int, context: Context): String {
        return when {
            followerCount < 1e3 -> followerCount.toString()
            followerCount < 1e6 -> {
                val count = (followerCount / 1e3).toInt()
                context.resources.getQuantityString(R.plurals.thousand, count, count)
            }
            else -> {
                val count = (followerCount / 1e6).toInt()
                context.resources.getQuantityString(R.plurals.million, count, count)
            }
        }
    }

    private fun getTimeString(followerCount: Int, context: Context): String {
        val seconds = followerCount / 200
        val minutes = seconds / 60
        val hours = minutes / 60
        val plural: Int
        val count: Int
        when {
            seconds > 60 -> {
                plural = R.plurals.minute
                count = minutes
            }
            minutes > 60 -> {
                plural = R.plurals.hour
                count = hours
            }
            hours > 24 -> {
                val days = hours / 24
                plural = R.plurals.day
                count = days
            }
            else -> {
                plural = R.plurals.second
                count = seconds
            }
        }
        return context.resources.getQuantityString(plural, count, count)
    }

    private fun addAccount(account: Account, context: Context) {
        doAsync {
            db = initialiseDb(applicationContext)
            db.accountDao().insertAll(account)
            db.close()

            uiThread {
                toast(getString(R.string.adding_username, account.username))
                accounts.add(account)
                Toast.makeText(context, getString(R.string.added, account.name), Toast.LENGTH_SHORT).show()
                accountList.adapter?.notifyDataSetChanged() //todo just notify there was one added
            }

            val intent = Intent(context, GetFollowersService::class.java)
            intent.putExtra("username", account.username)
            intent.putExtra("id", account.id)
            startService(intent)
        }

    }
}
