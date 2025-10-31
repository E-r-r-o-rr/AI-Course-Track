// ui/course_detail/LessonsAdapter.kt
package com.example.automation.course_detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.automation.R
import com.example.automation.databinding.ItemLessonBinding
import com.example.automation.model.Lesson
import com.example.automation.model.LessonStatus

class LessonsAdapter(
    private val onStart: (Lesson) -> Unit
) : ListAdapter<Lesson, LessonsAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Lesson>() {
            override fun areItemsTheSame(o: Lesson, n: Lesson) = o.id == n.id
            override fun areContentsTheSame(o: Lesson, n: Lesson) = o == n
        }
    }

    inner class VH(val vb: ItemLessonBinding) : RecyclerView.ViewHolder(vb.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vb = ItemLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(vb)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)
        with(h.vb) {
            title.text = item.title
            due.text   = item.dueText
            chipStatus.text = item.status.name

            val color = when (item.status) {
                LessonStatus.TODO  -> R.color.gray_700
                LessonStatus.DOING -> R.color.brand_500
                LessonStatus.DONE  -> R.color.green_primary
            }
            chipStatus.chipStrokeColor = ContextCompat.getColorStateList(root.context, color)
            chipStatus.setTextColor(ContextCompat.getColor(root.context, color))

            btnStart.setOnClickListener { onStart(item) }
        }
    }
}
