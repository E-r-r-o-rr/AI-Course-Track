package com.example.automation.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.automation.R
import com.example.automation.databinding.FragmentDashboardBinding
import com.example.automation.ui.AppViewModelFactory
import com.example.automation.ui.DashboardViewModel
import com.example.automation.ui.ThemeViewModel
import com.example.automation.ui.theme.updateThemeMenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigationrail.NavigationRailView

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModelFactory: AppViewModelFactory
    private val viewModel: DashboardViewModel by viewModels { viewModelFactory }
    private lateinit var themeViewModel: ThemeViewModel
    private lateinit var currentTaskAdapter: DashboardItemAdapter
    private lateinit var queuedAdapter: DashboardItemAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModelFactory = AppViewModelFactory(requireActivity().application)
        themeViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ThemeViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.title = getString(R.string.dashboard_title)
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

        currentTaskAdapter = DashboardItemAdapter(
            onClick = { item ->
                findNavController().navigate(
                    R.id.action_dashboardFragment_to_learningDetailFragment,
                    Bundle().apply { putLong("itemId", item.id) }
                )
            },
            actionsProvider = {
                DashboardItemActions(
                    primary = DashboardItemAction(
                        textRes = R.string.complete_learning_item,
                        onClick = { viewModel.completeItem(it) }
                    ),
                    secondary = DashboardItemAction(
                        textRes = R.string.move_to_queue,
                        onClick = { viewModel.moveToQueue(it) }
                    ),
                    tertiary = DashboardItemAction(
                        textRes = R.string.remove_from_queue,
                        iconRes = R.drawable.ic_delete_24,
                        onClick = { viewModel.removeFromQueue(it) }
                    )
                )
            }
        )
        binding.currentTaskList?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = currentTaskAdapter
            isNestedScrollingEnabled = false
        }

        queuedAdapter = DashboardItemAdapter(
            onClick = { item ->
                findNavController().navigate(
                    R.id.action_dashboardFragment_to_learningDetailFragment,
                    Bundle().apply { putLong("itemId", item.id) }
                )
            },
            actionsProvider = {
                DashboardItemActions(
                    primary = DashboardItemAction(
                        textRes = R.string.start_learning_item,
                        onClick = { viewModel.startItem(it) }
                    ),
                    secondary = DashboardItemAction(
                        iconRes = R.drawable.ic_delete_24,
                        contentDescriptionRes = R.string.remove_from_queue_content_description,
                        onClick = { viewModel.removeFromQueue(it) }
                    )
                )
            }
        )
        binding.queuedList?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = queuedAdapter
            isNestedScrollingEnabled = false
        }

        val weeklyGoalViews = listOfNotNull(
            binding.weeklyGoalCircle1,
            binding.weeklyGoalCircle2,
            binding.weeklyGoalCircle3,
            binding.weeklyGoalCircle4,
            binding.weeklyGoalCircle5,
        )

        viewModel.summary.observe(viewLifecycleOwner) { summary ->
            binding.totalCount?.text = summary.total.toString()
            binding.doneCount?.text = summary.done.toString()
            binding.progressCount?.text = summary.inProgress.toString()

            val completedForGoal = summary.done.coerceAtMost(weeklyGoalViews.size)
            weeklyGoalViews.forEachIndexed { index, view ->
                val drawableRes = if (index < completedForGoal) {
                    R.drawable.bg_weekly_goal_circle_filled
                } else {
                    R.drawable.bg_weekly_goal_circle_empty
                }
                view.background = ContextCompat.getDrawable(requireContext(), drawableRes)
            }
            binding.weeklyGoalContainer?.contentDescription = getString(
                R.string.weekly_goal_progress_content_description,
                completedForGoal,
                weeklyGoalViews.size
            )
        }

        viewModel.currentTasks.observe(viewLifecycleOwner) { list ->
            currentTaskAdapter.submitList(list)
            binding.emptyCurrentTask?.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.queuedItems.observe(viewLifecycleOwner) { list ->
            queuedAdapter.submitList(list)
            binding.emptyQueued?.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.viewLibraryButton?.let { button ->
            button.setOnClickListener {
                val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
                val navRail = requireActivity().findViewById<NavigationRailView>(R.id.navRail)
                when {
                    navRail != null && navRail.visibility == View.VISIBLE -> {
                        navRail.selectedItemId = R.id.learningListFragment
                    }
                    bottomNav != null -> {
                        bottomNav.selectedItemId = R.id.learningListFragment
                    }
                }
            }
        }

        binding.createItemButton?.let { button ->
            button.setOnClickListener {
                findNavController().navigate(
                    R.id.action_dashboardFragment_to_learningEditFragment,
                    Bundle().apply { putLong("itemId", 0L) }
                )
            }
        }

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
