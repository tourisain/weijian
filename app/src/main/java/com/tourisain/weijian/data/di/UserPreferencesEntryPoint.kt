package com.tourisain.weijian.data.di

import com.tourisain.weijian.data.preferences.UserPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
@EntryPoint
@InstallIn(ActivityComponent::class)
interface UserPreferencesEntryPoint {
    fun userPreferences(): UserPreferences
}
