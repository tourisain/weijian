package com.tourisain.weijian.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
private const val APP_PREFERENCES = "app_preferences"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = APP_PREFERENCES
)
