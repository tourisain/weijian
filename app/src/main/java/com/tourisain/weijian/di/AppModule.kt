package com.tourisain.weijian.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tourisain.weijian.data.database.AppDatabase
import com.tourisain.weijian.data.database.dao.AccountDao
import com.tourisain.weijian.data.database.dao.CategoryDao
import com.tourisain.weijian.data.database.dao.NoteDao
import com.tourisain.weijian.data.database.dao.NoteRevisionDao
import com.tourisain.weijian.data.database.dao.UserDao
import com.tourisain.weijian.data.database.dao.NoteCategoryDao
import com.tourisain.weijian.data.repository.NoteCategoryRepository
import com.tourisain.weijian.util.ActivationCodeManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "memo_app_db"
        )
        .addMigrations(
            AppDatabase.MIGRATION_74_75,
            AppDatabase.MIGRATION_75_76,
            AppDatabase.MIGRATION_76_77,
            AppDatabase.MIGRATION_77_78,
            AppDatabase.LEGACY_FEATURE_CLEANUP_MIGRATION_78_79
        )
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .build()
    }

    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideNoteRevisionDao(db: AppDatabase): NoteRevisionDao = db.noteRevisionDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideNoteCategoryDao(db: AppDatabase): NoteCategoryDao = db.noteCategoryDao()

    @Provides
    @Singleton
    fun provideNoteCategoryRepository(noteCategoryDao: NoteCategoryDao): NoteCategoryRepository {
        return NoteCategoryRepository(noteCategoryDao)
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDao: NoteDao,
        noteRevisionDao: NoteRevisionDao
    ): com.tourisain.weijian.data.repository.NoteRepository {
        return com.tourisain.weijian.data.repository.NoteRepository(noteDao, noteRevisionDao)
    }

    @Provides
    @Singleton
    fun provideAlarmScheduler(app: Application): com.tourisain.weijian.util.AlarmScheduler {
        return com.tourisain.weijian.util.AlarmScheduler(app)
    }

    @Provides
    @Singleton
    fun provideUserPreferences(app: Application): com.tourisain.weijian.data.preferences.UserPreferences {
        return com.tourisain.weijian.data.preferences.UserPreferences(app)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao, 
        db: AppDatabase,
        userPreferences: com.tourisain.weijian.data.preferences.UserPreferences, 
        membershipStateProtector: com.tourisain.weijian.util.MembershipStateProtector,
        @ApplicationContext context: Context
    ): com.tourisain.weijian.data.repository.UserRepository {
        return com.tourisain.weijian.data.repository.UserRepository(
            userDao,
            db,
            userPreferences,
            membershipStateProtector,
            context
        )
    }

    @Provides
    @Singleton
    fun providePremiumManager(
        userPreferences: com.tourisain.weijian.data.preferences.UserPreferences, 
        userRepository: com.tourisain.weijian.data.repository.UserRepository
    ): com.tourisain.weijian.util.PremiumManager {
        return com.tourisain.weijian.util.PremiumManager(userPreferences, userRepository)
    }

    @Provides
    @Singleton
    fun provideFinancialAnalyzer(): com.tourisain.weijian.util.FinancialAnalyzer {
        return com.tourisain.weijian.util.FinancialAnalyzer()
    }

    @Provides
    @Singleton
    fun provideActivationCodeManager(
        @ApplicationContext context: Context,
        userPreferences: com.tourisain.weijian.data.preferences.UserPreferences,
        userRepository: com.tourisain.weijian.data.repository.UserRepository,
        securityEnvironmentMonitor: com.tourisain.weijian.util.SecurityEnvironmentMonitor
    ): ActivationCodeManager {
        return ActivationCodeManager(context, userPreferences, userRepository, securityEnvironmentMonitor)
    }

    @Provides
    @Singleton
    fun provideBackupRepository(
        db: AppDatabase,
        userPreferences: com.tourisain.weijian.data.preferences.UserPreferences,
        @ApplicationContext context: Context
    ): com.tourisain.weijian.data.repository.BackupRepository {
        return com.tourisain.weijian.data.repository.BackupRepository(db, userPreferences, context)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NoteCategoryRepositoryEntryPoint {
    fun noteCategoryRepository(): NoteCategoryRepository
    fun userRepository(): com.tourisain.weijian.data.repository.UserRepository
    fun noteRepository(): com.tourisain.weijian.data.repository.NoteRepository
}



