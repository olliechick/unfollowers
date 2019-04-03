package me.olliechick.instagramunfollowers

import android.arch.persistence.room.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.Database

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey var id: Int,
    var username: String,
    var name: String,
    var created: OffsetDateTime
)

@Entity(tableName = "followers", primaryKeys = ["id", "timestamp"],
    foreignKeys = [ForeignKey(entity=Account::class, parentColumns = ["id"], childColumns = ["following_id"])])
data class Follower(
    var id: Int,
    var timestamp: OffsetDateTime,
    var username: String,
    var name: String,
    @ColumnInfo(name = "following_id") var followingId: Int
)

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY created")
    fun getAll(): List<Account>

    @Insert
    fun insertAll(vararg users: Account)
}

@Dao
interface FollowerDao {
    @Query("SELECT * FROM followers WHERE following_id = :followingId")
    fun getAllFollowersOfAUser(followingId: Int): List<Follower>

    @Query("SELECT * FROM followers JOIN accounts on followers.following_id = accounts.id WHERE followers.username = :followingUsername")
    fun getAllFollowersOfAUser(followingUsername: String): List<Follower>
}

@Database(entities = [Account::class, Follower::class], version = 1)
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

//