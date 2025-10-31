package com.example.automation.courses

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.automation.R
import com.example.automation.databinding.FragmentCoursesBinding
import com.example.automation.ui.AppVMFactory
import com.example.automation.ui.CoursesViewModel

class CoursesFragment : Fragment() {
    private var _vb: FragmentCoursesBinding? = null
    private val vb get() = _vb!!
    private val vm: CoursesViewModel by viewModels { AppVMFactory(requireActivity().application) }

    private lateinit var adapter: CoursesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _vb = FragmentCoursesBinding.inflate(inflater, container, false)
        return vb.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = CoursesAdapter { course ->
            findNavController().navigate(R.id.action_courses_to_courseDetail, Bundle().apply {
                putLong("courseId", course.id)
            })
        }
        vb.rvCourses.layoutManager = LinearLayoutManager(requireContext())
        vb.rvCourses.adapter = adapter

        vm.courses.observe(viewLifecycleOwner) { list -> adapter.submitList(list) }
    }

    override fun onDestroyView() { _vb = null; super.onDestroyView() }
}
