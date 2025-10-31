package com.example.automation.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.automation.R
import com.example.automation.databinding.FragmentDashboardBinding
import com.example.automation.ui.AppViewModelFactory
import com.example.automation.ui.DashboardViewModel

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels { AppViewModelFactory(requireActivity().application) }
    private lateinit var adapter: NextUpAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.title = getString(R.string.dashboard_title)

        adapter = NextUpAdapter { item ->
            findNavController().navigate(
                R.id.action_dashboardFragment_to_learningDetailFragment,
                Bundle().apply { putLong("itemId", item.id) }
            )
        }
        binding.nextUpList.layoutManager = LinearLayoutManager(requireContext())
        binding.nextUpList.adapter = adapter

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
            findNavController().navigate(R.id.action_dashboardFragment_to_learningListFragment)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
