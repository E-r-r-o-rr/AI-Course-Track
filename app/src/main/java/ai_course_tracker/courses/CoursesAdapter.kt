// ui/courses/CoursesAdapter.kt
package com.example.automation.courses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.example.automation.databinding.ItemCourseBinding
import com.example.automation.model.Course

class CoursesAdapter(
    private val onOpen: (Course) -> Unit
) : ListAdapter<Course, CoursesAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Course>() {
            override fun areItemsTheSame(o: Course, n: Course) = o.id == n.id
            override fun areContentsTheSame(o: Course, n: Course) = o == n
        }
    }

    inner class VH(val vb: ItemCourseBinding) : RecyclerView.ViewHolder(vb.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vb = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(vb)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.vb) {
            txtTitle.text = item.title
            txtProvider.text = "${item.provider} • ${item.lessons} lessons"
            progress.setProgress(item.progressPercent, /*animate=*/true)

            // Chips
            chips.removeAllViews()
            item.tags.take(3).forEach { t ->
                val c = Chip(root.context, null, com.google.android.material.R.attr.chipStyle)
                c.text = t
                c.isCheckable = false
                c.isClickable = false
                chips.addView(c)
            }

            btnOpen.setOnClickListener { onOpen(item) }
        }
    }
}
