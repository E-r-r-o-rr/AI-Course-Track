package com.example.automation.ui.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.automation.R
import com.example.automation.databinding.ItemBrowseBinding
import com.example.automation.model.BrowseSuggestion
import com.example.automation.ui.BrowseUiModel

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
            binding.title.text = suggestion.title
            val subtitleParts = buildList {
                if (suggestion.source.isNotBlank()) add(suggestion.source)
                suggestion.duration?.takeIf { it.isNotBlank() }?.let { add(it) }
            }
            binding.source.text = subtitleParts.joinToString(" • ")
            binding.source.isVisible = binding.source.text.isNotBlank()
            binding.description.text = suggestion.description

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
