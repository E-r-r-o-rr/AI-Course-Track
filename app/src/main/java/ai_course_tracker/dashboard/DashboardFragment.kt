// app/src/main/java/com/example/automation/ui/dashboard/DashboardFragment.kt
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
import com.example.automation.model.Lesson
import com.example.automation.ui.AppVMFactory
import com.example.automation.ui.DashboardViewModel

class DashboardFragment : Fragment() {
    private var _vb: FragmentDashboardBinding? = null
    private val vb get() = _vb!!

    private val vm: DashboardViewModel by viewModels { AppVMFactory(requireActivity().application) }
    private lateinit var adapter: NextUpAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _vb = FragmentDashboardBinding.inflate(inflater, container, false)
        return vb.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vb.toolbar.setNavigationOnClickListener { /* optional */ }

        adapter = NextUpAdapter { lesson: Lesson ->
            findNavController().navigate(R.id.sessionLogFragment, Bundle().apply {
                putLong("lessonId", lesson.id)
                putString("lessonTitle", lesson.title)
            })
        }
        vb.rvNextUp.layoutManager = LinearLayoutManager(requireContext())
        vb.rvNextUp.adapter = adapter

        // Live stats from VM
        vm.totalMinutes.observe(viewLifecycleOwner) { vb.txtTotalTime.text = it }
        vm.sessionsWeek.observe(viewLifecycleOwner) { vb.txtSessionsWeek.text = it.toString() }

        // “Next up” fed from DB (TODO/DOING ordered by due date, then recent)
        vm.nextUp.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    override fun onDestroyView() { _vb = null; super.onDestroyView() }
}
