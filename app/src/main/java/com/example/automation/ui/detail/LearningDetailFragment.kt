package com.example.automation.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.automation.R
import com.example.automation.databinding.FragmentLearningDetailBinding
import com.example.automation.model.LearningStatus
import com.example.automation.ui.AppViewModelFactory
import com.example.automation.ui.LearningDetailViewModel

class LearningDetailFragment : Fragment() {
    private var _binding: FragmentLearningDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LearningDetailViewModel by viewModels { AppViewModelFactory(requireActivity().application) }
    private var itemId: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLearningDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        itemId = requireArguments().getLong("itemId")
        viewModel.load(itemId)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.inflateMenu(R.menu.menu_learning_detail)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    findNavController().navigate(
                        R.id.action_learningDetailFragment_to_learningEditFragment,
                        Bundle().apply { putLong("itemId", itemId) }
                    )
                    true
                }

                R.id.action_delete -> {
                    confirmDelete()
                    true
                }

                else -> false
            }
        }

        binding.statusRadio.setOnCheckedChangeListener { _, checkedId ->
            val status = when (checkedId) {
                R.id.radioTodo -> LearningStatus.TODO
                R.id.radioDoing -> LearningStatus.IN_PROGRESS
                R.id.radioDone -> LearningStatus.DONE
                else -> null
            }
            val current = viewModel.item.value?.status
            if (status != null && status != current) {
                viewModel.updateStatus(status)
            }
        }

        binding.openButton.setOnClickListener {
            viewModel.item.value?.url?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                val pm = requireContext().packageManager
                if (intent.resolveActivity(pm) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), R.string.no_browser, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.notesEdit.doAfterTextChanged { text ->
            viewModel.updateNote(text?.toString().orEmpty())
        }

        viewModel.item.observe(viewLifecycleOwner) { item ->
            if (item == null) {
                findNavController().navigateUp()
                return@observe
            }
            binding.toolbar.title = item.title
            binding.source.text = getString(R.string.detail_source_format, item.source)
            binding.statusRadio.check(
                when (item.status) {
                    LearningStatus.TODO -> R.id.radioTodo
                    LearningStatus.IN_PROGRESS -> R.id.radioDoing
                    LearningStatus.DONE -> R.id.radioDone
                }
            )
            if (binding.notesEdit.text.toString() != item.note) {
                binding.notesEdit.setText(item.note)
            }
            binding.tagsGroup.removeAllViews()
            item.tags.forEach { tag ->
                val chip = layoutInflater.inflate(R.layout.view_tag_chip, binding.tagsGroup, false) as com.google.android.material.chip.Chip
                chip.text = tag
                binding.tagsGroup.addView(chip)
            }
            binding.tagsGroup.isVisible = item.tags.isNotEmpty()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_message)
            .setPositiveButton(android.R.string.ok) { _, _ -> viewModel.deleteItem() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
