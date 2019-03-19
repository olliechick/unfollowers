package me.olliechick.instagramunfollowers

import android.arch.persistence.room.*

@Entity
data class Account(
    @PrimaryKey var id: Int,
    @ColumnInfo(name = "username") var username: String,
    @ColumnInfo(name = "name") var name: String
)

@Dao
interface AccountDao {

    @Query("SELECT * FROM account")
    fun getAll(): List<Account>

    @Insert
    fun insertAll(vararg users: Account)

//    fun get_url(username: String): String {
//        return "https://www.instagram.com/$username/"
//    }
}

@Database(entities = arrayOf(Account::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}
/*

@Entity
data class UserAccount(
    @PrimaryKey var uid: Int,
    @ColumnInfo(name = "first_name") var firstName: String?,
    @ColumnInfo(name = "last_name") var lastName: String?
)


@Dao
interface UserAccountDao {
    @Query("SELECT * FROM userAccount")
    fun getAll(): List<UserAccount>

    @Query("SELECT * FROM userAccount WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<UserAccount>

    @Query(
        "SELECT * FROM user WHERE first_name LIKE :first AND " +
                "last_name LIKE :last LIMIT 1"
    )
    fun findByName(first: String, last: String): UserAccount

    @Insert
    fun insertAll(vararg users: UserAccount)

    @Delete
    fun delete(user: UserAccount)
}*/
