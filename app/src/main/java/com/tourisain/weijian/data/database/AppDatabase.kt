package com.tourisain.weijian.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tourisain.weijian.data.database.dao.*
import com.tourisain.weijian.data.database.entity.*

@Database(
    entities = [
        NoteEntity::class,
        UserEntity::class,
        AccountRecordEntity::class,
        CategoryEntity::class,
        NoteCategoryEntity::class,
        NoteRevisionEntity::class
    ],
    version = 79,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun noteCategoryDao(): NoteCategoryDao
    abstract fun noteRevisionDao(): NoteRevisionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "memo_app_db"
                )
                .addMigrations(
                    MIGRATION_74_75,
                    MIGRATION_75_76,
                    MIGRATION_76_77,
                    MIGRATION_77_78,
                    LEGACY_FEATURE_CLEANUP_MIGRATION_78_79
                )
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .build().also { instance = it }
            }
        }

        val MIGRATION_74_75 = object : Migration(74, 75) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN deleted_at INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_user_deleted ON notes(user_id, is_deleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_user_deleted_created ON notes(user_id, is_deleted, created_at)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_deleted_time ON notes(is_deleted, deleted_at)")
            }
        }

        val MIGRATION_75_76 = object : Migration(75, 76) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE account_records ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE account_records ADD COLUMN deleted_at INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_account_records_user_id ON account_records(user_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_account_records_user_deleted ON account_records(user_id, is_deleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_account_records_user_deleted_date ON account_records(user_id, is_deleted, date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_account_records_deleted_time ON account_records(is_deleted, deleted_at)")
            }
        }

        val MIGRATION_76_77 = object : Migration(76, 77) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN is_locked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_user_locked ON notes(user_id, is_locked)")
            }
        }

        val MIGRATION_77_78 = object : Migration(77, 78) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS note_revisions (
                        id TEXT NOT NULL PRIMARY KEY,
                        note_id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        color TEXT NOT NULL,
                        is_pinned INTEGER NOT NULL,
                        skin_id TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        saved_at INTEGER NOT NULL,
                        format TEXT NOT NULL,
                        tags TEXT,
                        type TEXT NOT NULL,
                        attachments TEXT,
                        category_id TEXT,
                        is_favorite INTEGER NOT NULL,
                        is_archived INTEGER NOT NULL,
                        is_locked INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_note_revisions_note_user ON note_revisions(note_id, user_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_note_revisions_user_saved ON note_revisions(user_id, saved_at)")
            }
        }

        val LEGACY_FEATURE_CLEANUP_MIGRATION_78_79 = object : Migration(78, 79) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf(
                    "countdowns",
                    "diaries",
                    "diary_templates",
                    "budgets",
                    "cards",
                    "courses",
                    "forests",
                    "trees",
                    "user_points",
                    "user_purchases",
                    "todos",
                    "markdown_documents"
                ).forEach { table ->
                    db.execSQL("DROP TABLE IF EXISTS $table")
                }
            }
        }
    }
}

