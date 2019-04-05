package me.olliechick.instagramunfollowers

import androidx.room.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey var id: Long,
    var username: String,
    var name: String,
    var created: OffsetDateTime
)

@Entity(tableName = "followers", primaryKeys = ["id", "timestamp"],
    foreignKeys = [ForeignKey(entity=Account::class, parentColumns = ["id"], childColumns = ["following_id"])])
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
}

@Dao
interface FollowerDao {
    @Query("SELECT * FROM followers")
    fun getAll(): List<Follower>

    @Query("SELECT * FROM followers WHERE following_id = :followingId")
    fun getAllFollowersOfAUser(followingId: Long): List<Follower>

    @Query("SELECT * FROM followers JOIN accounts on followers.following_id = accounts.id WHERE followers.username = :followingUsername")
    fun getAllFollowersOfAUser(followingUsername: String): List<Follower>

    @Query("""SELECT id, timestamp, username, name, following_id
                    FROM (SELECT * FROM followers
                          WHERE following_id = :followingId) AS t1
                    LEFT JOIN (SELECT id as current_id FROM followers
                               WHERE following_id = :followingId
                               AND timestamp = (SELECT MAX(timestamp) FROM followers WHERE following_id = :followingId)) AS t2
                    ON (id = current_id)
                    WHERE current_id IS NULL
                    GROUP BY id""")
    fun getUnfollowersOfAUser(followingId: Long): List<Follower>

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