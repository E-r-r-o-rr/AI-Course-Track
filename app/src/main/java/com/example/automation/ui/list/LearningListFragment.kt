package com.example.automation.ui.list

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.automation.R
import com.example.automation.databinding.FragmentLearningListBinding
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import com.example.automation.ui.AppViewModelFactory
import com.example.automation.ui.LearningListViewModel
import com.example.automation.ui.ThemeViewModel
import com.example.automation.ui.theme.updateThemeMenuItem
import com.google.android.material.chip.Chip

class LearningListFragment : Fragment() {
    private var _binding: FragmentLearningListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModelFactory: AppViewModelFactory
    private val viewModel: LearningListViewModel by viewModels { viewModelFactory }
    private lateinit var themeViewModel: ThemeViewModel
    private lateinit var adapter: LearningListAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModelFactory = AppViewModelFactory(requireActivity().application)
        themeViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ThemeViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLearningListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setTitle(R.string.library_title)
        binding.toolbar.inflateMenu(R.menu.menu_learning_list)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add -> {
                    openCreateForm()
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

        adapter = LearningListAdapter(
            onItemClick = { openDetail(it) },
            onToggleStatus = { viewModel.toggleStatus(it) },
            onAddToQueue = { viewModel.addToQueue(it) },
            onDelete = { viewModel.deleteItem(it) }
        )

        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.recyclerView.adapter = adapter

        // ⬇️ Safe-call because createLearningItem is nullable in some layout variants
        binding.createLearningItem?.setOnClickListener { openCreateForm() }

        binding.emptyCreateButton?.setOnClickListener { openCreateForm() }

        // ⬇️ Guard optional views that may not exist in every layout
        binding.statusChips?.setOnCheckedStateChangeListener { _, checkedIds ->
            val status = when (checkedIds.firstOrNull()) {
                R.id.chipTodo -> LearningStatus.TODO
                R.id.chipDoing -> LearningStatus.IN_PROGRESS
                R.id.chipDone -> LearningStatus.DONE
                else -> null
            }
            viewModel.setStatusFilter(status)
        }

        binding.tagGroup?.setOnCheckedStateChangeListener { group, checkedIds ->
            val tag = checkedIds.firstOrNull()?.let { id ->
                group.findViewById<View>(id)?.tag as? String
            }
            viewModel.setTagFilter(tag)
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            binding.emptyState.isVisible = items.isEmpty()
            adapter.submitList(items)
        }

        viewModel.availableTags.observe(viewLifecycleOwner) { tags ->
            val tagGroup = binding.tagGroup
            if (tagGroup == null) {
                // Layout without tag group — still keep filter state sane.
                if (tags.isEmpty()) viewModel.setTagFilter(null)
                return@observe
            }

            val previousSelection = tagGroup.checkedChipId
                .takeIf { it != View.NO_ID }
                ?.let { id -> tagGroup.findViewById<View>(id)?.tag as? String }

            tagGroup.removeAllViews()
            tags.forEach { tag ->
                val chipView = layoutInflater.inflate(R.layout.view_filter_chip, tagGroup, false)
                chipView.tag = tag
                if (chipView is Chip) {
                    chipView.id = View.generateViewId()
                    chipView.text = tag
                    if (previousSelection != null && tag.equals(previousSelection, ignoreCase = true)) {
                        chipView.isChecked = true
                    }
                }
                tagGroup.addView(chipView)
            }

            binding.tagHeader?.isVisible = tags.isNotEmpty()
            tagGroup.isVisible = tags.isNotEmpty()

            if (previousSelection != null && tags.none { it.equals(previousSelection, ignoreCase = true) }) {
                viewModel.setTagFilter(null)
            }
            if (tags.isEmpty()) {
                tagGroup.clearCheck()
                viewModel.setTagFilter(null)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun openCreateForm() {
        findNavController().navigate(
            R.id.action_learningListFragment_to_learningEditFragment,
            Bundle().apply { putLong("itemId", 0L) }
        )
    }

    private fun openDetail(item: LearningItem) {
        findNavController().navigate(
            R.id.action_learningListFragment_to_learningDetailFragment,
            Bundle().apply { putLong("itemId", item.id) }
        )
    }
}
