package com.example.automation.ui.dashboard

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.automation.R
import com.example.automation.databinding.ItemNextUpBinding
import com.example.automation.model.LearningItem
import com.example.automation.ui.category.iconRes
import com.example.automation.ui.category.labelRes
import com.example.automation.ui.category.tintRes
import com.google.android.material.button.MaterialButton

class DashboardItemAdapter(
    private val onClick: (LearningItem) -> Unit,
    private val actionsProvider: (LearningItem) -> DashboardItemActions
) :
    ListAdapter<LearningItem, DashboardItemAdapter.ViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<LearningItem>() {
        override fun areItemsTheSame(oldItem: LearningItem, newItem: LearningItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: LearningItem, newItem: LearningItem) = oldItem == newItem
    }

    inner class ViewHolder(val binding: ItemNextUpBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNextUpBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            title.text = item.title
            val categoryLabel = holder.itemView.context.getString(item.category.labelRes())
            val sourceText = item.source.takeIf { it.isNotBlank() }
            subtitle.text = if (sourceText.isNullOrBlank()) {
                categoryLabel
            } else {
                holder.itemView.context.getString(
                    R.string.next_up_subtitle_format,
                    categoryLabel,
                    sourceText
                )
            }
            categoryIcon.setImageResource(item.category.iconRes())
            val tintColor = ContextCompat.getColor(holder.itemView.context, item.category.tintRes())
            categoryIcon.backgroundTintList = ColorStateList.valueOf(tintColor)
            categoryIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, android.R.color.white)
            )
            categoryIcon.contentDescription = holder.itemView.context.getString(
                R.string.category_icon_content_description,
                categoryLabel
            )
            root.setOnClickListener { onClick(item) }

            val actions = actionsProvider(item)
            primaryAction.applyAction(item, actions.primary)
            secondaryAction.applyAction(item, actions.secondary)
            tertiaryAction.applyAction(item, actions.tertiary)
            actionRow.isVisible = listOfNotNull(actions.primary, actions.secondary, actions.tertiary).isNotEmpty()
        }
    }

    private fun MaterialButton.applyAction(item: LearningItem, action: DashboardItemAction?) {
        if (action == null) {
            isVisible = false
            setOnClickListener(null)
            return
        }

        isVisible = true
        isEnabled = true
        val resolvedText = action.text ?: action.textRes?.let { context.getString(it) } ?: ""
        text = resolvedText
        isAllCaps = false
        if (action.iconRes != null) {
            icon = ResourcesCompat.getDrawable(resources, action.iconRes, context.theme)
            iconGravity = if (resolvedText.isBlank()) {
                MaterialButton.ICON_GRAVITY_START
            } else {
                MaterialButton.ICON_GRAVITY_TEXT_START
            }
        } else {
            icon = null
        }
        contentDescription = action.contentDescription
            ?: action.contentDescriptionRes?.let { context.getString(it) }
            ?: if (resolvedText.isNotBlank()) resolvedText else null
        setOnClickListener { action.onClick(item) }
    }
}

data class DashboardItemActions(
    val primary: DashboardItemAction? = null,
    val secondary: DashboardItemAction? = null,
    val tertiary: DashboardItemAction? = null
)

data class DashboardItemAction(
    @StringRes val textRes: Int? = null,
    val text: CharSequence? = null,
    @DrawableRes val iconRes: Int? = null,
    @StringRes val contentDescriptionRes: Int? = null,
    val contentDescription: CharSequence? = null,
    val onClick: (LearningItem) -> Unit
)
