package com.example.automation.data.db

import androidx.room.TypeConverter
import com.example.automation.model.LearningStatus

class Converters {
    @TypeConverter
    fun fromTags(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split("|").map { it.trim() }

    @TypeConverter
    fun toTags(tags: List<String>): String = tags.joinToString(separator = "|")

    @TypeConverter
    fun toStatus(value: String): LearningStatus = LearningStatus.valueOf(value)

    @TypeConverter
    fun fromStatus(status: LearningStatus): String = status.name
}
