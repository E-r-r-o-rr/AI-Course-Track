package com.example.automation.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.automation.R
import com.example.automation.databinding.FragmentLearningEditBinding
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import com.example.automation.ui.AppViewModelFactory
import com.example.automation.ui.LearningEditViewModel
import kotlinx.coroutines.launch

class LearningEditFragment : Fragment() {
    private var _binding: FragmentLearningEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LearningEditViewModel by viewModels { AppViewModelFactory(requireActivity().application) }

    private var currentItem: LearningItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLearningEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val itemId = requireArguments().getLong("itemId")
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        if (itemId != 0L) {
            viewLifecycleOwner.lifecycleScope.launch {
                currentItem = viewModel.loadForEdit(itemId)
                currentItem?.let { populate(it) }
            }
        } else {
            binding.toolbar.title = getString(R.string.create_title)
        }

        binding.saveButton.setOnClickListener {
            val title = binding.titleInput.editText?.text?.toString().orEmpty()
            val url = binding.urlInput.editText?.text?.toString().orEmpty()
            val source = binding.sourceInput.editText?.text?.toString().orEmpty()
            val tags = binding.tagsInput.editText?.text?.toString().orEmpty()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val status = when (binding.statusChipGroup.checkedChipId) {
                R.id.chipStatusTodo -> LearningStatus.TODO
                R.id.chipStatusDoing -> LearningStatus.IN_PROGRESS
                R.id.chipStatusDone -> LearningStatus.DONE
                else -> LearningStatus.TODO
            }

            if (title.isBlank() || url.isBlank() || source.isBlank()) {
                Toast.makeText(requireContext(), R.string.validation_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val base = currentItem
            val item = if (base == null) {
                LearningItem(
                    title = title,
                    url = url,
                    source = source,
                    tags = tags,
                    status = status,
                    note = "",
                    addedAt = System.currentTimeMillis(),
                    completedAt = if (status == LearningStatus.DONE) System.currentTimeMillis() else null
                )
            } else {
                base.copy(
                    title = title,
                    url = url,
                    source = source,
                    tags = tags,
                    status = status,
                    completedAt = if (status == LearningStatus.DONE) base.completedAt ?: System.currentTimeMillis() else null
                )
            }

            viewModel.save(item)
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun populate(item: LearningItem) {
        binding.toolbar.title = getString(R.string.edit_title)
        binding.titleInput.editText?.setText(item.title)
        binding.urlInput.editText?.setText(item.url)
        binding.sourceInput.editText?.setText(item.source)
        binding.tagsInput.editText?.setText(item.tags.joinToString(", "))
        binding.statusChipGroup.check(
            when (item.status) {
                LearningStatus.TODO -> R.id.chipStatusTodo
                LearningStatus.IN_PROGRESS -> R.id.chipStatusDoing
                LearningStatus.DONE -> R.id.chipStatusDone
            }
        )
    }
}
