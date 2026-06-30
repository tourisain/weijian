package com.tourisain.weijian.di

import android.content.Context
import com.tourisain.weijian.util.ReminderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object ReminderModule {
    @Provides
    @Singleton
    fun provideReminderManager(
        @ApplicationContext context: Context
    ): ReminderManager {
        return ReminderManager(context)
    }
}
