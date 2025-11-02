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
    @ColorRes val iconTintRes: Int = textColorRes,
    @ColorRes val strokeColorRes: Int? = null,
    val strokeWidthDp: Int = 0
) {
    START(
        R.color.brand_400,
        android.R.color.white
    ),
    COMPLETE(
        R.color.brand_400,
        android.R.color.white
    ),
    DELETE(R.color.action_destructive, android.R.color.white),
    REMOVE_FROM_CURRENT(
        R.color.action_neutral_background,
        R.color.primaryText,
        R.color.primaryText,
        R.color.glass_chip_stroke,
        strokeWidthDp = 1
    ),
    QUEUE(R.color.brand_400, android.R.color.white),
    NEUTRAL(
        R.color.action_neutral_background,
        R.color.primaryText,
        R.color.primaryText
    ),
    QUEUE_DISABLED(
        R.color.action_neutral_background,
        R.color.secondaryText,
        R.color.secondaryText,
        R.color.glass_stroke,
        strokeWidthDp = 1
    )
}

fun Chip.applyActionStyle(style: ActionStyle) {
    val context = context
    chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, style.backgroundColorRes))
    setTextColor(ContextCompat.getColor(context, style.textColorRes))
    if (style.strokeWidthDp > 0 && style.strokeColorRes != null) {
        chipStrokeWidth = style.strokeWidthDp.dpToPx(context)
        chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, style.strokeColorRes))
    } else {
        chipStrokeWidth = 0f
        chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.transparent))
    }
    chipIconTint = ColorStateList.valueOf(ContextCompat.getColor(context, style.iconTintRes))
}

fun MaterialButton.applyActionStyle(style: ActionStyle) {
    val context = context
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, style.backgroundColorRes))
    setTextColor(ContextCompat.getColor(context, style.textColorRes))
    if (style.strokeWidthDp > 0 && style.strokeColorRes != null) {
        strokeWidth = style.strokeWidthDp.dpToPxInt(context)
        strokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, style.strokeColorRes))
    } else {
        strokeWidth = 0
        strokeColor = null
    }
}

private fun Int.dpToPx(context: Context): Float {
    if (this <= 0) return 0f
    val px = this * context.resources.displayMetrics.density
    return if (px < 1f) 1f else px
}

private fun Int.dpToPxInt(context: Context): Int {
    if (this <= 0) return 0
    val px = this * context.resources.displayMetrics.density
    return if (px < 1f) 1 else px.toInt()
}

fun resolveActionStyle(context: Context, label: CharSequence?): ActionStyle {
    val value = label?.toString()?.trim()?.lowercase() ?: return ActionStyle.NEUTRAL
    return when (value) {
        context.getString(R.string.start_learning_item).lowercase() -> ActionStyle.START
        context.getString(R.string.delete_learning_item).lowercase() -> ActionStyle.DELETE
        context.getString(R.string.complete_learning_item).lowercase() -> ActionStyle.COMPLETE
        context.getString(R.string.remove_from_current).lowercase() -> ActionStyle.REMOVE_FROM_CURRENT
        context.getString(R.string.add_to_queue).lowercase() -> ActionStyle.QUEUE
        context.getString(R.string.in_queue).lowercase() -> ActionStyle.QUEUE_DISABLED
        else -> ActionStyle.NEUTRAL
    }
}

fun Chip.applyActionStyleByLabel(label: CharSequence?) {
    applyActionStyle(resolveActionStyle(context, label))
}

fun MaterialButton.applyActionStyleByLabel(label: CharSequence?) {
    applyActionStyle(resolveActionStyle(context, label))
}
