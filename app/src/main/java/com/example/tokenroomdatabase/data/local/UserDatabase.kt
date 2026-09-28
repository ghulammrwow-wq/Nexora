package com.example.tokenroomdatabase.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tokenroomdatabase.data.model.Documentary
import com.example.tokenroomdatabase.data.model.User

@Database(
    entities = [
        User::class,
        Documentary::class
    ],
    version = 5,
    exportSchema = false
)
abstract class UserDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun documentaryDao(): DocumentaryDao

    companion object {

        @Volatile
        private var INSTANCE: UserDatabase? = null


        // ==================== 1 -> 2 ====================

        val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(
                database: SupportSQLiteDatabase
            ) {

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS documentaries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        documentUri TEXT NOT NULL,
                        isFavorite INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }


        // ==================== 2 -> 3 ====================

        val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(
                database: SupportSQLiteDatabase
            ) {

                database.execSQL(
                    """
                    ALTER TABLE documentaries
                    ADD COLUMN userEmail TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE documentaries
                    ADD COLUMN userImageUri TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )
            }
        }


        // ==================== 3 -> 4 ====================

        val MIGRATION_3_4 = object : Migration(3, 4) {

            override fun migrate(
                database: SupportSQLiteDatabase
            ) {

                database.execSQL(
                    """
                    ALTER TABLE documentaries
                    ADD COLUMN mediaType TEXT NOT NULL DEFAULT 'pdf'
                    """.trimIndent()
                )
            }
        }


        // ==================== 4 -> 5 ====================

        val MIGRATION_4_5 = object : Migration(4, 5) {

            override fun migrate(
                database: SupportSQLiteDatabase
            ) {

                database.execSQL(
                    """
                    ALTER TABLE documentaries
                    ADD COLUMN username TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )
            }
        }


        // ==================== DATABASE ====================

        fun getDatabase(
            context: Context
        ): UserDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        UserDatabase::class.java,
                        "user_database"
                    )
                        .addMigrations(
                            MIGRATION_1_2,
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5
                        )
                        .build()

                INSTANCE = instance

                instance
            }
        }
    }
}