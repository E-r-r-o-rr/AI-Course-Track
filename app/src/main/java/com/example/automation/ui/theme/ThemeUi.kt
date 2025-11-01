package com.example.automation.ui.theme

import android.content.Context
import android.content.res.Configuration
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import com.example.automation.R

fun isNightMode(context: Context, mode: Int): Boolean {
    return when (mode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}

fun updateThemeMenuItem(context: Context, item: MenuItem?, mode: Int) {
    if (item == null) return
    val isNight = isNightMode(context, mode)
    val icon = if (isNight) R.drawable.ic_light_mode_24 else R.drawable.ic_dark_mode_24
    val title = if (isNight) R.string.action_enable_light else R.string.action_enable_dark
    item.setIcon(icon)
    item.title = context.getString(title)
}
