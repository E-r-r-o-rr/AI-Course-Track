package com.example.automation.ui.edit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.automation.R
import com.example.automation.databinding.FragmentLearningEditBinding
import com.example.automation.model.LearningCategory
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import com.example.automation.ui.AppViewModelFactory
import com.example.automation.ui.LearningEditViewModel
import com.example.automation.ui.ThemeViewModel
import com.example.automation.ui.category.iconRes
import com.example.automation.ui.theme.updateThemeMenuItem
import kotlinx.coroutines.launch

class LearningEditFragment : Fragment() {
    private var _binding: FragmentLearningEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModelFactory: AppViewModelFactory
    private val viewModel: LearningEditViewModel by viewModels { viewModelFactory }
    private lateinit var themeViewModel: ThemeViewModel

    private var currentItem: LearningItem? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModelFactory = AppViewModelFactory(requireActivity().application)
        themeViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ThemeViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLearningEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val itemId = requireArguments().getLong("itemId")
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.inflateMenu(R.menu.menu_theme_only)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
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

        applyCategoryIcons()

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
            val source = binding.sourceInput.editText?.text?.toString().orEmpty()
            val tags = binding.tagsInput.editText?.text?.toString().orEmpty()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val category = when (binding.categoryToggle.checkedButtonId) {
                R.id.buttonCategoryCourse -> LearningCategory.COURSE
                R.id.buttonCategoryVideo -> LearningCategory.VIDEO
                R.id.buttonCategoryBook -> LearningCategory.BOOK
                R.id.buttonCategoryPodcast -> LearningCategory.PODCAST
                else -> LearningCategory.COURSE
            }
            val status = when (binding.statusToggle.checkedButtonId) {
                R.id.buttonStatusTodo -> LearningStatus.TODO
                R.id.buttonStatusDoing -> LearningStatus.IN_PROGRESS
                R.id.buttonStatusDone -> LearningStatus.DONE
                else -> LearningStatus.TODO
            }

            if (title.isBlank() || source.isBlank()) {
                Toast.makeText(requireContext(), R.string.validation_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val base = currentItem
            val retainedUrl = base?.url ?: ""
            val item = if (base == null) {
                LearningItem(
                    title = title,
                    url = retainedUrl,
                    source = source,
                    category = category,
                    tags = tags,
                    status = status,
                    note = "",
                    addedAt = System.currentTimeMillis(),
                    completedAt = if (status == LearningStatus.DONE) System.currentTimeMillis() else null
                )
            } else {
                base.copy(
                    title = title,
                    url = retainedUrl,
                    source = source,
                    category = category,
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
        binding.sourceInput.editText?.setText(item.source)
        binding.tagsInput.editText?.setText(item.tags.joinToString(", "))
        binding.categoryToggle.check(
            when (item.category) {
                LearningCategory.COURSE -> R.id.buttonCategoryCourse
                LearningCategory.VIDEO -> R.id.buttonCategoryVideo
                LearningCategory.BOOK -> R.id.buttonCategoryBook
                LearningCategory.PODCAST -> R.id.buttonCategoryPodcast
            }
        )
        binding.statusToggle.check(
            when (item.status) {
                LearningStatus.TODO -> R.id.buttonStatusTodo
                LearningStatus.IN_PROGRESS -> R.id.buttonStatusDoing
                LearningStatus.DONE -> R.id.buttonStatusDone
            }
        )
    }

    private fun applyCategoryIcons() {
        binding.buttonCategoryCourse.setIconResource(LearningCategory.COURSE.iconRes())
        binding.buttonCategoryVideo.setIconResource(LearningCategory.VIDEO.iconRes())
        binding.buttonCategoryBook.setIconResource(LearningCategory.BOOK.iconRes())
        binding.buttonCategoryPodcast.setIconResource(LearningCategory.PODCAST.iconRes())
    }
}
