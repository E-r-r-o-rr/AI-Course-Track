package com.example.automation.ui.dashboard

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.automation.databinding.ItemNextUpBinding
import com.example.automation.model.LearningItem
import com.example.automation.ui.category.iconRes
import com.example.automation.ui.category.labelRes
import com.example.automation.ui.category.tintRes
import com.example.automation.R

class NextUpAdapter(private val onClick: (LearningItem) -> Unit) :
    ListAdapter<LearningItem, NextUpAdapter.ViewHolder>(Diff) {

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
        }
    }
}
