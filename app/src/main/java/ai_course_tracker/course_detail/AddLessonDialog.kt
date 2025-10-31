// app/src/main/java/com/example/automation/ui/course_detail/AddLessonDialog.kt
package com.example.automation.ui.course_detail

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddLessonDialog(private val onAdd: (String) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val ctx = requireContext()

        // Build a simple Material text field programmatically (no XML, no findViewById)
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(20), dp(24), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }

        val til = TextInputLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            hint = "Lesson title"
        }
        val edit = TextInputEditText(ctx).apply {
            inputType = InputType.TYPE_CLASS_TEXT
        }
        til.addView(edit)
        container.addView(til)

        return AlertDialog.Builder(ctx)
            .setTitle("Add lesson")
            .setView(container)
            .setPositiveButton("Add") { _, _ ->
                val title = edit.text?.toString()?.trim().orEmpty()
                if (title.isNotEmpty()) onAdd(title)
                // (If empty, we just dismiss; add validation if you want to block)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun dp(value: Int): Int =
        (value * requireContext().resources.displayMetrics.density).toInt()
}
