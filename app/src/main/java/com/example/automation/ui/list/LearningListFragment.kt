package com.example.automation.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.automation.R
import com.example.automation.databinding.FragmentLearningListBinding
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import com.example.automation.ui.AppViewModelFactory
import com.example.automation.ui.LearningListViewModel

class LearningListFragment : Fragment() {
    private var _binding: FragmentLearningListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LearningListViewModel by viewModels { AppViewModelFactory(requireActivity().application) }
    private lateinit var adapter: LearningListAdapter

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
                    findNavController().navigate(
                        R.id.action_learningListFragment_to_learningEditFragment,
                        Bundle().apply { putLong("itemId", 0L) }
                    )
                    true
                }

                else -> false
            }
        }

        adapter = LearningListAdapter(
            onItemClick = { openDetail(it) },
            onToggleStatus = { viewModel.toggleStatus(it) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.statusChips.setOnCheckedStateChangeListener { _, checkedIds ->
            val status = when (checkedIds.firstOrNull()) {
                R.id.chipTodo -> LearningStatus.TODO
                R.id.chipDoing -> LearningStatus.IN_PROGRESS
                R.id.chipDone -> LearningStatus.DONE
                else -> null
            }
            viewModel.setStatusFilter(status)
        }

        binding.tagGroup.setOnCheckedStateChangeListener { group, checkedIds ->
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
            val previousSelection = binding.tagGroup.checkedChipId.takeIf { it != View.NO_ID }?.let { id ->
                binding.tagGroup.findViewById<View>(id)?.tag as? String
            }
            binding.tagGroup.removeAllViews()
            tags.forEach { tag ->
                val chip = layoutInflater.inflate(R.layout.view_filter_chip, binding.tagGroup, false)
                chip.tag = tag
                if (chip is com.google.android.material.chip.Chip) {
                    chip.id = View.generateViewId()
                    chip.text = tag
                    if (previousSelection != null && tag.equals(previousSelection, ignoreCase = true)) {
                        chip.isChecked = true
                    }
                }
                binding.tagGroup.addView(chip)
            }
            binding.tagHeader.isVisible = tags.isNotEmpty()
            binding.tagGroup.isVisible = tags.isNotEmpty()
            if (previousSelection != null && tags.none { it.equals(previousSelection, ignoreCase = true) }) {
                viewModel.setTagFilter(null)
            }
            if (tags.isEmpty()) {
                binding.tagGroup.clearCheck()
                viewModel.setTagFilter(null)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun openDetail(item: LearningItem) {
        findNavController().navigate(
            R.id.action_learningListFragment_to_learningDetailFragment,
            Bundle().apply { putLong("itemId", item.id) }
        )
    }
}
