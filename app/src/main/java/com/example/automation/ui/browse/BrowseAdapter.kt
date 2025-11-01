package com.example.automation.ui.browse

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.automation.R
import com.example.automation.databinding.ItemBrowseBinding
import com.example.automation.model.BrowseSuggestion
import com.example.automation.ui.BrowseUiModel
import com.example.automation.ui.category.iconRes
import com.example.automation.ui.category.labelRes
import com.example.automation.ui.category.tintRes

class BrowseAdapter(
    private val onPreview: (BrowseSuggestion) -> Unit,
    private val onAdd: (BrowseSuggestion) -> Unit
) : ListAdapter<BrowseUiModel, BrowseAdapter.BrowseViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBrowseBinding.inflate(inflater, parent, false)
        return BrowseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BrowseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BrowseViewHolder(private val binding: ItemBrowseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BrowseUiModel) {
            val suggestion = item.suggestion
            val context = binding.root.context
            binding.title.text = suggestion.title
            val categoryLabel = context.getString(suggestion.category.labelRes())
            val subtitleParts = buildList {
                if (suggestion.source.isNotBlank()) add(suggestion.source)
                suggestion.duration?.takeIf { it.isNotBlank() }?.let { add(it) }
            }
            val secondary = subtitleParts.joinToString(" • ")
            binding.source.text = if (secondary.isBlank()) {
                categoryLabel
            } else {
                context.getString(R.string.browse_meta_format, categoryLabel, secondary)
            }
            binding.source.isVisible = binding.source.text.isNotBlank()
            binding.description.text = suggestion.description

            binding.categoryIcon.setImageResource(suggestion.category.iconRes())
            val tintColor = ContextCompat.getColor(context, suggestion.category.tintRes())
            binding.categoryIcon.backgroundTintList = ColorStateList.valueOf(tintColor)
            binding.categoryIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, android.R.color.white)
            )
            binding.categoryIcon.contentDescription = context.getString(
                R.string.category_icon_content_description,
                categoryLabel
            )

            binding.tagGroup.removeAllViews()
            val inflater = LayoutInflater.from(binding.tagGroup.context)
            suggestion.tags.forEach { tag ->
                val chip = inflater.inflate(R.layout.view_tag_chip, binding.tagGroup, false)
                if (chip is com.google.android.material.chip.Chip) {
                    chip.text = tag
                }
                binding.tagGroup.addView(chip)
            }
            binding.tagGroup.isVisible = suggestion.tags.isNotEmpty()

            binding.openButton.setOnClickListener { onPreview(suggestion) }

            if (item.alreadyAdded) {
                binding.addButton.isEnabled = false
                binding.addButton.text = binding.root.context.getString(R.string.browse_added)
                binding.addButton.alpha = 0.6f
                binding.addButton.setOnClickListener(null)
            } else {
                binding.addButton.isEnabled = true
                binding.addButton.text = binding.root.context.getString(R.string.browse_add)
                binding.addButton.alpha = 1f
                binding.addButton.setOnClickListener { onAdd(suggestion) }
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<BrowseUiModel>() {
        override fun areItemsTheSame(oldItem: BrowseUiModel, newItem: BrowseUiModel): Boolean =
            oldItem.suggestion.url == newItem.suggestion.url

        override fun areContentsTheSame(oldItem: BrowseUiModel, newItem: BrowseUiModel): Boolean =
            oldItem == newItem
    }
}
