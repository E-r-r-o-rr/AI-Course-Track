package com.example.automation.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LearningStatus { TODO, IN_PROGRESS, DONE }

enum class LearningCategory { COURSE, VIDEO, BOOK, PODCAST }

@Entity(tableName = "learning_items")
data class LearningItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val source: String,
    val category: LearningCategory,
    val tags: List<String>,
    val status: LearningStatus,
    val note: String,
    val addedAt: Long,
    val completedAt: Long?
)
