package com.example.automation.ui.detail

import android.content.Context
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
    private var originalStatus: LearningStatus? = null
    private var pendingStatus: LearningStatus? = null
    private var isProgrammaticStatusUpdate = false

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
            if (!isChecked || isProgrammaticStatusUpdate) return@addOnButtonCheckedListener
            val status = when (checkedId) {
                R.id.buttonTodo -> LearningStatus.TODO
                R.id.buttonDoing -> LearningStatus.IN_PROGRESS
                R.id.buttonDone -> LearningStatus.DONE
                else -> null
            }
            if (status != null) {
                pendingStatus = status
                updateSaveChangesButtonState()
            }
        }

        binding.saveChangesButton?.isEnabled = false
        binding.saveChangesButton?.setOnClickListener {
            val noteToSave = pendingNote
            val statusToSave = pendingStatus ?: originalStatus ?: viewModel.item.value?.status
            if (statusToSave != null) {
                viewModel.saveChanges(noteToSave, statusToSave)
                originalStatus = statusToSave
                pendingStatus = statusToSave
            } else {
                viewModel.updateNote(noteToSave)
            }
            originalNote = noteToSave
            pendingNote = noteToSave
            binding.saveChangesButton?.isEnabled = false
            Toast.makeText(requireContext(), R.string.changes_saved, Toast.LENGTH_SHORT).show()
        }

        binding.notesEdit.doAfterTextChanged { text ->
            if (isProgrammaticNoteUpdate) return@doAfterTextChanged
            pendingNote = text?.toString().orEmpty()
            updateSaveChangesButtonState()
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
            val persistedStatus = item.status
            val hasPendingStatusChange = pendingStatus != null && pendingStatus != persistedStatus
            originalStatus = persistedStatus
            if (!hasPendingStatusChange) {
                pendingStatus = persistedStatus
                val desiredId = persistedStatus.toButtonId()
                if (binding.statusToggle.checkedButtonId != desiredId) {
                    isProgrammaticStatusUpdate = true
                    binding.statusToggle.check(desiredId)
                    isProgrammaticStatusUpdate = false
                }
            } else {
                val desiredId = pendingStatus?.toButtonId()
                if (desiredId != null && binding.statusToggle.checkedButtonId != desiredId) {
                    isProgrammaticStatusUpdate = true
                    binding.statusToggle.check(desiredId)
                    isProgrammaticStatusUpdate = false
                }
            }

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
            updateSaveChangesButtonState()
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

    private fun updateSaveChangesButtonState() {
        val statusChanged = pendingStatus != null && originalStatus != null && pendingStatus != originalStatus
        val noteChanged = pendingNote != originalNote
        binding.saveChangesButton?.isEnabled = statusChanged || noteChanged
    }

    private fun LearningStatus.toButtonId(): Int = when (this) {
        LearningStatus.TODO -> R.id.buttonTodo
        LearningStatus.IN_PROGRESS -> R.id.buttonDoing
        LearningStatus.DONE -> R.id.buttonDone
    }
}
