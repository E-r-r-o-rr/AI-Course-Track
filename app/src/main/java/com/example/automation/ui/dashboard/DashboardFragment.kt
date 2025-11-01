package com.example.automation.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var adapter: NextUpAdapter

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

        adapter = NextUpAdapter { item ->
            findNavController().navigate(
                R.id.action_dashboardFragment_to_learningDetailFragment,
                Bundle().apply { putLong("itemId", item.id) }
            )
        }
        binding.nextUpList.layoutManager = LinearLayoutManager(requireContext())
        binding.nextUpList.adapter = adapter
        binding.nextUpList.isNestedScrollingEnabled = false

        viewModel.summary.observe(viewLifecycleOwner) { summary ->
            binding.totalCount.text = summary.total.toString()
            binding.doneCount.text = summary.done.toString()
            binding.progressCount.text = summary.inProgress.toString()
        }

        viewModel.nextUp.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.emptyNextUp.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.openLibrary.setOnClickListener {
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
