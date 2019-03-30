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
import kotlinx.android.synthetic.main.activity_account_list.*
import me.olliechick.instagramunfollowers.MyApplication.Companion.logout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast


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

    var accounts: List<Account> = listOf()
        set(value) {
            field = value
            accountList.adapter = AccountAdapter(this, field) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.instagram.com/${it.username}/")
                ) //todo change to open unfollower list
                startActivity((intent))
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

    private fun populateList() {
        accountList = findViewById(R.id.accountList)
        val layoutManager = LinearLayoutManager(this)
        accountList.layoutManager = layoutManager

        doAsync {

            db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "db"
            ).build()
            val dao = db.accountDao()

            accounts = db.accountDao().getAll()

            if (accounts.isEmpty()) dao.insertAll(
                Account(1, "ollienickchick", "Ollie Chick"),
                Account(2, "instagram", "Instagram"),
                Account(3, "adam", "adam"),
                Account(4, "george", "george"),
                Account(5, "shosahna", "shosahna")
            )

            accounts = db.accountDao().getAll()


        }

        val decoration = DividerItemDecoration(this, layoutManager.orientation)
        accountList.addItemDecoration(decoration)
    }

    private fun openAddAccountDialog() {
        var accountName: String
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
        ) { dialog, which -> addAccount(input.text.toString()) }

        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun addAccount(accountName: String) {
        toast("This is where I would add $accountName.")

    }
}
