package me.olliechick.instagramunfollowers

import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_account_list.*
import me.olliechick.instagramunfollowers.MyApplication.Companion.getAccount
import me.olliechick.instagramunfollowers.MyApplication.Companion.helpUrl
import me.olliechick.instagramunfollowers.MyApplication.Companion.logout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread


class AccountListActivity : AppCompatActivity() {
    private lateinit var accountList: RecyclerView
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_list)

        val actionBar = supportActionBar
        actionBar?.title = "Accounts"


        fab.setOnClickListener { openAddAccountDialog() }

        populateList()
    }

    var accounts: ArrayList<Account> = arrayListOf()
        set(value) {
            field = value
            accountList.adapter = AccountAdapter(this, field) {
                val intent = Intent(this, UnfollowersListActivity::class.java)
                intent.putExtra("username", it.username)
                startActivity(intent)
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.mainmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            toast("Settings page not yet implemented")
//          val intent = Intent(this, Settings::class.java)
//          startActivity(intent)
            true
        }

        R.id.action_help -> {
            val uri = Uri.parse(helpUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            true
        }


        R.id.action_log_out -> {
            val prefs = getSharedPreferences(MyApplication.prefsFile, Context.MODE_PRIVATE)
            logout(prefs)

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun getAccountDao(): AccountDao {
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "db"
        ).build()
        return db.accountDao()
    }

    private fun populateList() {
        accountList = findViewById(R.id.accountList)
        val layoutManager = LinearLayoutManager(this)
        accountList.layoutManager = layoutManager

        doAsync {
            accounts = ArrayList(getAccountDao().getAll())
        }

        val decoration = DividerItemDecoration(this, layoutManager.orientation)
        accountList.addItemDecoration(decoration)
    }

    private fun openAddAccountDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Add an account to track:")

        // Set up the input
        val input = EditText(this)

        // Specify the type of input expected
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton(
            "Add"
        ) { _, _ -> addAccount(input.text.toString()) }

        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun addAccount(username: String) {
        toast("Adding $username...")
        val context = this
        val dao = getAccountDao()
        doAsync {
            val result = getAccount(username)
            if (result.status.toLowerCase() == "ok") {
                val user = result.user
                val name = user.full_name
                val id = user.pk.toInt()
                val newAccount = Account(id, username, name)
                dao.insertAll(
                    newAccount
                )
                accounts.add(newAccount)
                uiThread {
                    Toast.makeText(context, "Added $name", Toast.LENGTH_SHORT).show()
                    accountList.adapter?.notifyDataSetChanged() //todo just notify there was one added
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
