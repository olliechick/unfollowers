package me.olliechick.instagramunfollowers

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey var id: Long,
    var username: String,
    var name: String,
    var created: OffsetDateTime,
    @ColumnInfo(name = "last_updated") var lastUpdated: OffsetDateTime
)

@Entity(
    tableName = "followers", primaryKeys = ["id", "timestamp"],
    foreignKeys = [ForeignKey(onDelete=CASCADE, entity = Account::class, parentColumns = ["id"], childColumns = ["following_id"])]
)
data class Follower(
    var id: Long,
    var timestamp: OffsetDateTime,
    var username: String,
    var name: String,
    @ColumnInfo(name = "following_id", index = true) var followingId: Long
)

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY created")
    fun getAll(): List<Account>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getUserFromId(id: Long): List<Account>

    @Query("SELECT id from accounts")
    fun getIds(): List<Long>

    @Insert
    fun insertAll(vararg users: Account)

    @Query("UPDATE accounts SET last_updated = :now WHERE id = :followingId")
    fun updateLastUpdated(followingId: Long, now: OffsetDateTime)

    @Query("SELECT last_updated FROM accounts WHERE id = :followingId")
    fun getLatestUpdateTime(followingId: Long): OffsetDateTime

    @Query("DELETE FROM accounts WHERE username = :username")
    fun delete(username: String)
}

@Dao
interface FollowerDao {
    @Query("SELECT * FROM followers")
    fun getAll(): List<Follower>

    @Query("SELECT * FROM followers WHERE following_id = :followingId")
    fun getAllFollowersOfAUser(followingId: Long): List<Follower>

    @Query("SELECT * FROM followers JOIN accounts on followers.following_id = accounts.id WHERE followers.username = :followingUsername")
    fun getAllFollowersOfAUser(followingUsername: String): List<Follower>

    @Query(
        """SELECT id, max(timestamp) as timestamp, username, name, following_id FROM followers
                  WHERE following_id = :followingId GROUP BY id"""
    )
    fun getLatestFollowersForEachId(followingId: Long): List<Follower>

    @Insert
    fun insertAll(vararg followers: Follower)
}

@Database(entities = [Account::class, Follower::class], version = 1, exportSchema = false)
@TypeConverters(TiviTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun followerDao(): FollowerDao
}

object TiviTypeConverters {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    @JvmStatic
    fun toOffsetDateTime(value: String?): OffsetDateTime? {
        return value?.let {
            return formatter.parse(value, OffsetDateTime::from)
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromOffsetDateTime(date: OffsetDateTime?): String? {
        return date?.format(formatter)
    }
}