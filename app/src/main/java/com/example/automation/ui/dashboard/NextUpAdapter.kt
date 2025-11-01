package com.example.automation.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.automation.databinding.ItemNextUpBinding
import com.example.automation.model.LearningItem

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
            subtitle.text = item.source
            root.setOnClickListener { onClick(item) }
        }
    }
}
