package com.example.automation.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.automation.data.Repository
import com.example.automation.model.Lesson
import com.example.automation.model.LessonStatus
import com.example.automation.data.CourseUi
import kotlinx.coroutines.launch

class AppVMFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = Repository.get(app)
        @Suppress("UNCHECKED_CAST")
        return when (modelClass) {
            CoursesViewModel::class.java      -> CoursesViewModel(repo)
            CourseDetailViewModel::class.java -> CourseDetailViewModel(repo)
            DashboardViewModel::class.java    -> DashboardViewModel(repo)
            SessionLogViewModel::class.java   -> SessionLogViewModel(repo)
            else -> throw IllegalArgumentException("Unknown VM ${modelClass.name}")
        } as T
    }
}

class CoursesViewModel(private val repo: Repository) : ViewModel() {
    val courses = repo.getCoursesUi().asLiveData()
}

class CourseDetailViewModel(private val repo: Repository) : ViewModel() {
    private val _courseId = MutableLiveData<Long>()
    val lessons: LiveData<List<Lesson>> = _courseId.switchMap { repo.getLessons(it).asLiveData() }

    fun load(courseId: Long) { _courseId.value = courseId }
    fun setStatus(lessonId: Long, status: LessonStatus) = viewModelScope.launch { repo.setLessonStatus(lessonId, status) }
    fun addLesson(courseId: Long, title: String) = viewModelScope.launch { repo.addLesson(courseId, title) }
}

class DashboardViewModel(private val repo: Repository) : ViewModel() {
    val totalMinutes = repo.totalDurationSec().asLiveData().map { (it / 60).toString() + "m" }
    val sessionsWeek = repo.sessionsThisWeek().asLiveData()
    val nextUp = repo.nextUp().asLiveData()
}

class SessionLogViewModel(private val repo: Repository) : ViewModel() {
    fun save(lessonId: Long, startedAt: Long, durationSec: Long, notes: String) =
        viewModelScope.launch { repo.saveSession(lessonId, startedAt, durationSec, notes) }
}
