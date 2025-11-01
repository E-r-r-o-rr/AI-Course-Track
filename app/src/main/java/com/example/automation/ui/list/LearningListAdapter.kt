package com.example.automation.ui.list

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.automation.R
import com.example.automation.databinding.ItemLearningBinding
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import kotlin.math.absoluteValue

class LearningListAdapter(
    private val onItemClick: (LearningItem) -> Unit,
    private val onToggleStatus: (LearningItem) -> Unit
) : ListAdapter<LearningItem, LearningListAdapter.ViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<LearningItem>() {
        override fun areItemsTheSame(oldItem: LearningItem, newItem: LearningItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: LearningItem, newItem: LearningItem) = oldItem == newItem
    }

    inner class ViewHolder(val binding: ItemLearningBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLearningBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            title.text = item.title
            source.text = holder.itemView.context.getString(R.string.list_source_format, item.source)
            statusChip.text = when (item.status) {
                LearningStatus.TODO -> holder.itemView.context.getString(R.string.status_todo)
                LearningStatus.IN_PROGRESS -> holder.itemView.context.getString(R.string.status_in_progress)
                LearningStatus.DONE -> holder.itemView.context.getString(R.string.status_done)
            }
            val colorRes = when (item.status) {
                LearningStatus.TODO -> R.color.statusTodo
                LearningStatus.IN_PROGRESS -> R.color.statusDoing
                LearningStatus.DONE -> R.color.statusDone
            }
            statusChip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, colorRes)
            )
            statusChip.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            statusChip.setOnClickListener { onToggleStatus(item) }
            tagsGroup.removeAllViews()
            item.tags.take(3).forEach { tag ->
                val chip = LayoutInflater.from(tagsGroup.context)
                    .inflate(R.layout.view_tag_chip, tagsGroup, false) as com.google.android.material.chip.Chip
                chip.text = tag
                tagsGroup.addView(chip)
            }
            tagsGroup.isVisible = item.tags.isNotEmpty()
            favicon.text = item.source.firstOrNull()?.uppercase() ?: "?"
            favicon.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(randomColorFor(holder.itemView.context, item.source))
            }
            root.setOnClickListener { onItemClick(item) }
        }
    }

    private fun randomColorFor(context: android.content.Context, source: String): Int {
        val colors = listOf(
            R.color.avatarBlue,
            R.color.avatarGreen,
            R.color.avatarPurple,
            R.color.avatarOrange
        )
        val index = (source.hashCode().absoluteValue) % colors.size
        return ContextCompat.getColor(context, colors[index])
    }
}
