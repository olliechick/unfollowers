package me.olliechick.instagramunfollowers

import android.arch.persistence.room.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.Database



@Entity(tableName = "Account")
data class Account(
    @PrimaryKey var id: Int,
    var username: String,
    var name: String,
    var created: OffsetDateTime
)

@Dao
interface AccountDao {
    @Query("SELECT * FROM account ORDER BY created")
    fun getAll(): List<Account>

    @Insert
    fun insertAll(vararg users: Account)
}

@Database(entities = arrayOf(Account::class), version = 1)
@TypeConverters(TiviTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
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