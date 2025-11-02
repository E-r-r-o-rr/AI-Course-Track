package com.example.automation.ui.list

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
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

        binding.tagSearchInput?.addTextChangedListener { editable ->
            viewModel.setTagQuery(editable?.toString())
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            binding.emptyState.isVisible = items.isEmpty()
            adapter.submitList(items)
        }

        viewModel.availableTags.observe(viewLifecycleOwner) { tags ->
            val searchLayout = binding.tagSearchLayout
            val searchInput = binding.tagSearchInput
            val hasTags = tags.isNotEmpty()
            searchLayout?.isVisible = hasTags
            if (!hasTags) {
                if (!searchInput?.text.isNullOrBlank()) {
                    searchInput?.setText("")
                }
                viewModel.setTagQuery(null)
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
