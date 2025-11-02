package com.example.automation.ui.detail

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import com.example.automation.ui.category.iconRes
import com.example.automation.ui.category.labelRes
import com.example.automation.ui.category.tintRes
import com.example.automation.ui.theme.updateThemeMenuItem
import com.google.android.material.chip.Chip

class LearningDetailFragment : Fragment() {
    private var _binding: FragmentLearningDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModelFactory: AppViewModelFactory
    private val viewModel: LearningDetailViewModel by viewModels { viewModelFactory }
    private lateinit var themeViewModel: ThemeViewModel
    private var itemId: Long = 0
    private var originalNote: String = ""
    private var pendingNote: String = ""
    private var isProgrammaticNoteUpdate = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModelFactory = AppViewModelFactory(requireActivity().application)
        themeViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ThemeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        binding.openButton?.setOnClickListener { handleOpenLink() }

        binding.saveChangesButton?.isEnabled = false
        binding.saveChangesButton?.setOnClickListener {
            val noteToSave = pendingNote
            viewModel.saveChanges(noteToSave)
            originalNote = noteToSave
            binding.saveChangesButton?.isEnabled = false
            Toast.makeText(requireContext(), R.string.changes_saved, Toast.LENGTH_SHORT).show()
        }

        binding.notesEdit.doAfterTextChanged { text ->
            if (isProgrammaticNoteUpdate) return@doAfterTextChanged
            pendingNote = text?.toString().orEmpty()
            binding.saveChangesButton?.isEnabled = pendingNote != originalNote
        }

        viewModel.item.observe(viewLifecycleOwner) { item ->
            if (item == null) {
                findNavController().navigateUp()
                return@observe
            }
            binding.toolbar.title = item.title
            binding.categoryIcon.setImageResource(item.category.iconRes())
            val tintColor = ContextCompat.getColor(requireContext(), item.category.tintRes())
            binding.categoryIcon.backgroundTintList = ColorStateList.valueOf(tintColor)
            binding.categoryIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), android.R.color.white)
            )
            binding.categoryLabel.text = getString(item.category.labelRes())
            binding.categoryIcon.contentDescription = getString(
                R.string.category_icon_content_description,
                binding.categoryLabel.text.toString()
            )
            binding.source.text = getString(R.string.detail_source_format, item.source)
            binding.source.isVisible = item.source.isNotBlank()
            binding.statusToggle.check(
                when (item.status) {
                    LearningStatus.TODO -> R.id.buttonTodo
                    LearningStatus.IN_PROGRESS -> R.id.buttonDoing
                    LearningStatus.DONE -> R.id.buttonDone
                }
            )

            val note = item.note
            val hasPendingNoteChanges = pendingNote != originalNote
            if (!hasPendingNoteChanges && binding.notesEdit.text.toString() != note) {
                isProgrammaticNoteUpdate = true
                binding.notesEdit.setText(note)
                binding.notesEdit.setSelection(binding.notesEdit.text?.length ?: 0)
                isProgrammaticNoteUpdate = false
            }

            if (!hasPendingNoteChanges) {
                originalNote = note
                pendingNote = note
                binding.saveChangesButton?.isEnabled = false
            } else {
                binding.saveChangesButton?.isEnabled = pendingNote != originalNote
            }

            binding.tagsGroup.removeAllViews()
            item.tags.forEach { tag ->
                val chipView = layoutInflater.inflate(
                    R.layout.view_tag_chip,
                    binding.tagsGroup,
                    false
                ) as Chip
                chipView.text = tag
                binding.tagsGroup.addView(chipView)
            }
            binding.tagsGroup.isVisible = item.tags.isNotEmpty()
            binding.openButton?.isEnabled = item.url.isNotBlank()
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

    private fun handleOpenLink() {
        val url = viewModel.item.value?.url?.takeIf { it.isNotBlank() }
        if (url.isNullOrBlank()) {
            Toast.makeText(requireContext(), R.string.no_link_available, Toast.LENGTH_SHORT).show()
            return
        }

        val parsedUri = android.net.Uri.parse(url)
        val launchUri = if (parsedUri.scheme.isNullOrEmpty()) {
            android.net.Uri.parse("https://$url")
        } else {
            parsedUri
        }

        val intent = Intent(Intent.ACTION_VIEW, launchUri)
        val pm = requireContext().packageManager
        if (intent.resolveActivity(pm) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), R.string.no_browser, Toast.LENGTH_SHORT).show()
        }
    }
}
