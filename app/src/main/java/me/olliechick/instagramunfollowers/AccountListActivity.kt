package me.olliechick.instagramunfollowers

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_account_list.*
import me.olliechick.instagramunfollowers.Util.Companion.getAccount
import me.olliechick.instagramunfollowers.Util.Companion.initialiseDb
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.time.OffsetDateTime


class AccountListActivity : AppCompatActivityWithMenu(), AddAccountDialogFragment.AddAccountDialogListener {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_list)

        val actionBar = supportActionBar
        actionBar?.title = getString(R.string.accounts)

        fab.setOnClickListener { openAddAccountDialog() }

        populateList()
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
            val allAccounts = db.accountDao().getAll()
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
        else {
            toast(getString(R.string.adding_username, username))
            val context = this
            doAsync {
                val result = getAccount(username)
                if (result.status.toLowerCase() == "ok") {
                    val user = result.user
                    val name = user.full_name
                    val id = user.pk
                    val created = OffsetDateTime.now()
                    val newAccount = Account(id, username, name, created, created)

                    db = initialiseDb(applicationContext)
                    db.accountDao().insertAll(newAccount)
                    db.close()

                    uiThread {
                        accounts.add(newAccount)
                        Toast.makeText(context, getString(R.string.added, name), Toast.LENGTH_SHORT).show()
                        accountList.adapter?.notifyDataSetChanged() //todo just notify there was one added
                    }

                    val intent = Intent(context, GetFollowersService::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("id", id)
                    startService(intent)

                } else {
                    uiThread {
                        Toast.makeText(context, getString(R.string.error_getting_account, username), Toast.LENGTH_SHORT)
                            .show()
                    }

                }

            }
        }
    }

    private fun usernameIsValid(username: String): Boolean {
        return username.length <= 30 && username.matches("[a-z0-9._]+".toRegex())
    }
}
