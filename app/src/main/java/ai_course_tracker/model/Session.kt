package com.example.automation.model

data class Session(
    val id: Long = 0L,
    val lessonId: Long,
    val startedAtMs: Long,
    val durationSec: Long,
    val notes: String
)
