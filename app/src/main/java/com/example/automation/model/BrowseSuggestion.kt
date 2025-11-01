package com.example.automation.model

data class BrowseSuggestion(
    val title: String,
    val source: String,
    val url: String,
    val description: String,
    val tags: List<String>,
    val duration: String? = null
)
