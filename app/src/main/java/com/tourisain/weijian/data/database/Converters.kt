package com.tourisain.weijian.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return if (value.isNullOrEmpty()) "[]" else gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty() || value == "null") return emptyList()
        return try {
            gson.fromJson<List<String>>(value, STRING_LIST_TYPE) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private companion object {
        val STRING_LIST_TYPE = object : TypeToken<List<String>>() {}.type
    }
}
