package com.example.automation.course_detail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.automation.R
import com.example.automation.databinding.FragmentCourseDetailBinding
import com.example.automation.model.Lesson
import com.example.automation.model.LessonStatus
import com.example.automation.AppVMFactory
import com.example.automation.CourseDetailViewModel

class CourseDetailFragment : Fragment() {
    private var _vb: FragmentCourseDetailBinding? = null
    private val vb get() = _vb!!
    private val vm: CourseDetailViewModel by viewModels { AppVMFactory(requireActivity().application) }

    private lateinit var adapter: LessonsAdapter
    private var courseId: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentCourseDetailBinding.inflate(inflater, container, false).also { _vb = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vb.toolbar.setNavigationOnClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }

        courseId = requireArguments().getLong("courseId")
        vm.load(courseId)

        adapter = LessonsAdapter { lesson: Lesson ->
            findNavController().navigate(
                R.id.action_courseDetail_to_sessionLog,
                Bundle().apply {
                    putLong("lessonId", lesson.id)
                    putString("lessonTitle", lesson.title)
                }
            )
        }
        vb.rvLessons.layoutManager = LinearLayoutManager(requireContext())
        vb.rvLessons.adapter = adapter

        vm.lessons.observe(viewLifecycleOwner) { adapter.submitList(it) }

        // quick status demo: long-press toggles DONE
        vb.rvLessons.addOnItemTouchListener(RecyclerItemClick(requireContext(), vb.rvLessons) { _, position ->
            val item = adapter.currentList[position]
            vm.setStatus(item.id, if (item.status == LessonStatus.DONE) LessonStatus.TODO else LessonStatus.DONE)
        })
    }

    override fun onDestroyView() { _vb = null; super.onDestroyView() }
}
