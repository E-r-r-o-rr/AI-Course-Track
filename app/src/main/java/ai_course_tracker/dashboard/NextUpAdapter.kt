package com.example.automation.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.automation.databinding.ItemNextUpBinding
import com.example.automation.model.Lesson

class NextUpAdapter(
    private val onStart: (Lesson) -> Unit
) : ListAdapter<Lesson, NextUpAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Lesson>() {
            override fun areItemsTheSame(o: Lesson, n: Lesson) = o.id == n.id
            override fun areContentsTheSame(o: Lesson, n: Lesson) = o == n
        }
    }

    inner class VH(val vb: ItemNextUpBinding) : RecyclerView.ViewHolder(vb.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vb = ItemNextUpBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(vb)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)
        h.vb.title.text = item.title
        h.vb.due.text = item.dueText
        h.vb.btnStart.setOnClickListener { onStart(item) }
    }
}
