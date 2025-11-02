package com.example.automation.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.automation.R
import com.example.automation.databinding.ItemNextUpBinding
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import com.example.automation.ui.category.iconRes
import com.example.automation.ui.category.labelRes
import com.example.automation.ui.category.tintRes
import com.example.automation.ui.common.ActionStyle
import com.example.automation.ui.common.applyActionStyle
import com.google.android.material.chip.Chip

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
            categoryIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(tintColor)
            categoryIcon.imageTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, android.R.color.white)
            )
            categoryIcon.contentDescription = holder.itemView.context.getString(
                R.string.category_icon_content_description,
                categoryLabel
            )
            root.setOnClickListener { onClick(item) }

            statusChip.isVisible = true
            statusChip.text = when (item.status) {
                LearningStatus.TODO -> holder.itemView.context.getString(R.string.status_todo)
                LearningStatus.IN_PROGRESS -> holder.itemView.context.getString(R.string.status_in_progress)
                LearningStatus.DONE -> holder.itemView.context.getString(R.string.status_done)
            }
            val statusColorRes = when (item.status) {
                LearningStatus.TODO -> R.color.statusTodo
                LearningStatus.IN_PROGRESS -> R.color.statusDoing
                LearningStatus.DONE -> R.color.statusDone
            }
            val statusColor = ContextCompat.getColor(holder.itemView.context, statusColorRes)
            statusChip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(statusColor)
            statusChip.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            statusChip.isCheckable = false
            statusChip.isChipIconVisible = false
            statusChip.isClickable = false
            statusChip.chipStrokeWidth = 0f
            statusChip.chipStrokeColor = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, android.R.color.transparent)
            )

            val actions = actionsProvider(item)
            primaryAction.applyAction(item, actions.primary)
            secondaryAction.applyAction(item, actions.secondary)
            tertiaryAction.applyAction(item, actions.tertiary)
            actionRow.isVisible = listOfNotNull(actions.primary, actions.secondary, actions.tertiary).isNotEmpty()

            val deleteAction = actions.delete
            deleteButton.isVisible = deleteAction != null
            if (deleteAction != null) {
                deleteButton.isEnabled = true
                deleteButton.applyActionStyle(deleteAction.style)
                val deleteIcon = deleteAction.iconRes?.let { iconRes ->
                    ResourcesCompat.getDrawable(holder.itemView.resources, iconRes, holder.itemView.context.theme)
                }
                if (deleteIcon != null) {
                    deleteButton.icon = deleteIcon
                }
                val deleteContentDescription = deleteAction.contentDescription
                    ?: deleteAction.contentDescriptionRes?.let { holder.itemView.context.getString(it) }
                    ?: deleteAction.text?.toString()
                    ?: deleteAction.textRes?.let { holder.itemView.context.getString(it) }
                    ?: holder.itemView.context.getString(R.string.delete_learning_item)
                deleteButton.contentDescription = deleteContentDescription
                ViewCompat.setTooltipText(deleteButton, deleteContentDescription)
                deleteButton.setOnClickListener { deleteAction.onClick(item) }
            } else {
                deleteButton.isEnabled = false
                deleteButton.setOnClickListener(null)
            }
        }
    }

    private fun Chip.applyAction(item: LearningItem, action: DashboardItemAction?) {
        if (action == null) {
            isVisible = false
            setOnClickListener(null)
            return
        }

        isVisible = true
        isEnabled = true
        val resolvedText = action.text ?: action.textRes?.let { context.getString(it) } ?: ""
        text = resolvedText
        isCheckable = false
        isChipIconVisible = false
        if (action.iconRes != null) {
            chipIcon = ResourcesCompat.getDrawable(resources, action.iconRes, context.theme)
            isChipIconVisible = true
        } else {
            chipIcon = null
        }
        applyActionStyle(action.style)
        if (isChipIconVisible) {
            chipIconTint = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(context, action.style.iconTintRes)
            )
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
    val tertiary: DashboardItemAction? = null,
    val delete: DashboardItemAction? = null
)

data class DashboardItemAction(
    @StringRes val textRes: Int? = null,
    val text: CharSequence? = null,
    @DrawableRes val iconRes: Int? = null,
    @StringRes val contentDescriptionRes: Int? = null,
    val contentDescription: CharSequence? = null,
    val style: ActionStyle = ActionStyle.NEUTRAL,
    val onClick: (LearningItem) -> Unit
)
