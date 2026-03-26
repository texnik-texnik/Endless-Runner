package com.endlessrunner.data.local.converter

import androidx.room.TypeConverter
import com.endlessrunner.domain.model.GraphicsQuality
import com.endlessrunner.domain.model.Orientation

/**
 * Конвертеры типов для Room Database.
 * Преобразует сложные типы в примитивы для хранения в БД.
 */
class TypeConverters {

    /**
     * Конвертация Set<String> в String (через запятую).
     */
    @TypeConverter
    fun fromStringSet(set: Set<String>): String = set.joinToString(separator = ",")

    /**
     * Конвертация String в Set<String>.
     */
    @TypeConverter
    fun toStringSet(string: String): Set<String> =
        if (string.isBlank()) emptySet()
        else string.split(",").filter { it.isNotBlank() }.toSet()

    /**
     * Конвертация GraphicsQuality в Int.
     */
    @TypeConverter
    fun fromGraphicsQuality(quality: GraphicsQuality): Int = quality.ordinal

    /**
     * Конвертация Int в GraphicsQuality.
     */
    @TypeConverter
    fun toGraphicsQuality(ordinal: Int): GraphicsQuality =
        GraphicsQuality.values().getOrNull(ordinal) ?: GraphicsQuality.MEDIUM

    /**
     * Конвертация Orientation в Int.
     */
    @TypeConverter
    fun fromOrientation(orientation: Orientation): Int = orientation.ordinal

    /**
     * Конвертация Int в Orientation.
     */
    @TypeConverter
    fun toOrientation(ordinal: Int): Orientation =
        Orientation.values().getOrNull(ordinal) ?: Orientation.LANDSCAPE

    /**
     * Конвертация List<String> в String (через точку с запятой).
     */
    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString(separator = ";")

    /**
     * Конвертация String в List<String>.
     */
    @TypeConverter
    fun toStringList(string: String): List<String> =
        if (string.isBlank()) emptyList()
        else string.split(";").filter { it.isNotBlank() }
}
