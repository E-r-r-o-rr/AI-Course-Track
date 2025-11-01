package com.example.automation.ui.category

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.automation.R
import com.example.automation.model.LearningCategory

@DrawableRes
fun LearningCategory.iconRes(): Int = when (this) {
    LearningCategory.COURSE -> R.drawable.ic_school_24
    LearningCategory.VIDEO -> R.drawable.ic_youtube_24
    LearningCategory.BOOK -> R.drawable.ic_book_24
    LearningCategory.PODCAST -> R.drawable.ic_podcast_24
}

@StringRes
fun LearningCategory.labelRes(): Int = when (this) {
    LearningCategory.COURSE -> R.string.category_course
    LearningCategory.VIDEO -> R.string.category_video
    LearningCategory.BOOK -> R.string.category_book
    LearningCategory.PODCAST -> R.string.category_podcast
}

@ColorRes
fun LearningCategory.tintRes(): Int = when (this) {
    LearningCategory.COURSE -> R.color.categoryCourse
    LearningCategory.VIDEO -> R.color.categoryVideo
    LearningCategory.BOOK -> R.color.categoryBook
    LearningCategory.PODCAST -> R.color.categoryPodcast
}
