package com.example.automation.ui.common

import android.content.Context
import android.content.res.ColorStateList
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.automation.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

enum class ActionStyle(
    @ColorRes val backgroundColorRes: Int,
    @ColorRes val textColorRes: Int,
    @ColorRes val iconTintRes: Int = textColorRes
) {
    START(R.color.action_positive, android.R.color.white),
    COMPLETE(R.color.action_positive, android.R.color.white),
    DELETE(R.color.action_destructive, android.R.color.white),
    REMOVE_FROM_CURRENT(R.color.action_move, android.R.color.white),
    NEUTRAL(R.color.action_neutral_background, R.color.primaryText, R.color.primaryText)
}

fun Chip.applyActionStyle(style: ActionStyle) {
    val context = context
    chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, style.backgroundColorRes))
    setTextColor(ContextCompat.getColor(context, style.textColorRes))
    chipStrokeWidth = 0f
    chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.transparent))
    chipIconTint = ColorStateList.valueOf(ContextCompat.getColor(context, style.iconTintRes))
}

fun MaterialButton.applyActionStyle(style: ActionStyle) {
    val context = context
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, style.backgroundColorRes))
    setTextColor(ContextCompat.getColor(context, style.textColorRes))
    strokeWidth = 0
    strokeColor = null
}

fun resolveActionStyle(context: Context, label: CharSequence?): ActionStyle {
    val value = label?.toString()?.trim()?.lowercase() ?: return ActionStyle.NEUTRAL
    return when (value) {
        context.getString(R.string.start_learning_item).lowercase() -> ActionStyle.START
        context.getString(R.string.delete_learning_item).lowercase() -> ActionStyle.DELETE
        context.getString(R.string.complete_learning_item).lowercase() -> ActionStyle.COMPLETE
        context.getString(R.string.remove_from_current).lowercase() -> ActionStyle.REMOVE_FROM_CURRENT
        else -> ActionStyle.NEUTRAL
    }
}

fun Chip.applyActionStyleByLabel(label: CharSequence?) {
    applyActionStyle(resolveActionStyle(context, label))
}

fun MaterialButton.applyActionStyleByLabel(label: CharSequence?) {
    applyActionStyle(resolveActionStyle(context, label))
}
