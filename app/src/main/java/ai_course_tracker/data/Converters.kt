package com.example.automation.data

import androidx.room.TypeConverter
import com.example.automation.model.LessonStatus

class Converters {
    @TypeConverter fun listToString(list: List<String>?): String = list?.joinToString("|") ?: ""
    @TypeConverter fun stringToList(s: String?): List<String> = if (s.isNullOrEmpty()) emptyList() else s.split("|")

    @TypeConverter fun statusToString(s: LessonStatus?): String = s?.name ?: LessonStatus.TODO.name
    @TypeConverter fun stringToStatus(s: String?): LessonStatus =
        runCatching { LessonStatus.valueOf(s ?: "TODO") }.getOrDefault(LessonStatus.TODO)
}
