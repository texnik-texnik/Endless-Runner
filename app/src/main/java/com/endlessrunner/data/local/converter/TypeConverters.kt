package com.endlessrunner.data.local.converter

import androidx.room.TypeConverter

/**
 * Конвертеры типов для Room Database.
 */
class TypeConverters {

    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString(separator = ";")

    @TypeConverter
    fun toStringList(string: String): List<String> =
        if (string.isBlank()) emptyList()
        else string.split(";").filter { it.isNotBlank() }
}
