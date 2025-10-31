package com.example.automation.ui.detail

import android.content.Context
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.automation.R
import com.example.automation.databinding.FragmentLearningDetailBinding
import com.example.automation.model.LearningStatus
import com.example.automation.ui.AppViewModelFactory
import com.example.automation.ui.LearningDetailViewModel
import com.example.automation.ui.ThemeViewModel
import com.example.automation.ui.theme.updateThemeMenuItem

class LearningDetailFragment : Fragment() {
    private var _binding: FragmentLearningDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModelFactory: AppViewModelFactory
    private val viewModel: LearningDetailViewModel by viewModels { viewModelFactory }
    private lateinit var themeViewModel: ThemeViewModel
    private var itemId: Long = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModelFactory = AppViewModelFactory(requireActivity().application)
        themeViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ThemeViewModel::class.java]
    }

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

                R.id.action_toggle_theme -> {
                    themeViewModel.toggleNightMode()
                    true
                }

                else -> false
            }
        }

        val themeItem = binding.toolbar.menu.findItem(R.id.action_toggle_theme)
        themeViewModel.themeMode.observe(viewLifecycleOwner) { mode ->
            updateThemeMenuItem(requireContext(), themeItem, mode)
        }

        binding.statusToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val status = when (checkedId) {
                R.id.buttonTodo -> LearningStatus.TODO
                R.id.buttonDoing -> LearningStatus.IN_PROGRESS
                R.id.buttonDone -> LearningStatus.DONE
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
            binding.statusToggle.check(
                when (item.status) {
                    LearningStatus.TODO -> R.id.buttonTodo
                    LearningStatus.IN_PROGRESS -> R.id.buttonDoing
                    LearningStatus.DONE -> R.id.buttonDone
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
