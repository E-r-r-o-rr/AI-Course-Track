package com.example.automation.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.automation.data.LearningRepository
import com.example.automation.data.preferences.ThemePreferences
import com.example.automation.model.BrowseSuggestion
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AppViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = LearningRepository.getInstance(app)
        val themePreferences = ThemePreferences.getInstance(app)
        @Suppress("UNCHECKED_CAST")
        return when (modelClass) {
            LearningListViewModel::class.java -> LearningListViewModel(repository)
            LearningDetailViewModel::class.java -> LearningDetailViewModel(repository)
            LearningEditViewModel::class.java -> LearningEditViewModel(repository)
            DashboardViewModel::class.java -> DashboardViewModel(repository)
            BrowseViewModel::class.java -> BrowseViewModel(repository)
            ThemeViewModel::class.java -> ThemeViewModel(themePreferences)
            else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
        } as T
    }
}

class LearningListViewModel(private val repository: LearningRepository) : ViewModel() {
    private val statusFilter = MutableStateFlow<LearningStatus?>(null)
    private val tagFilter = MutableStateFlow<String?>(null)

    private val combined = combine(
        repository.observeItems(),
        statusFilter,
        tagFilter
    ) { items, status, tag ->
        items.filter { item ->
            (status == null || item.status == status) &&
                (tag.isNullOrBlank() || item.tags.any { it.equals(tag, ignoreCase = true) })
        }
    }

    val items = combined.asLiveData()
    val availableTags = repository.observeDistinctTags().asLiveData()

    fun setStatusFilter(status: LearningStatus?) {
        statusFilter.value = status
    }

    fun setTagFilter(tag: String?) {
        tagFilter.value = tag
    }

    fun toggleStatus(item: LearningItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val nextStatus = when (item.status) {
                LearningStatus.TODO -> LearningStatus.IN_PROGRESS
                LearningStatus.IN_PROGRESS -> LearningStatus.DONE
                LearningStatus.DONE -> LearningStatus.TODO
            }
            repository.updateStatus(item.id, nextStatus)
        }
    }
}

class LearningDetailViewModel(private val repository: LearningRepository) : ViewModel() {
    private val itemId = MutableStateFlow<Long?>(null)
    val item = itemId.combine(repository.observeItems()) { id, items ->
        items.firstOrNull { it.id == id }
    }.asLiveData()

    fun load(id: Long) {
        itemId.value = id
    }

    fun updateNote(note: String) {
        val id = itemId.value ?: return
        viewModelScope.launch(Dispatchers.IO) { repository.updateNote(id, note) }
    }

    fun updateStatus(status: LearningStatus) {
        val id = itemId.value ?: return
        viewModelScope.launch(Dispatchers.IO) { repository.updateStatus(id, status) }
    }

    fun deleteItem() {
        val current = item.value ?: return
        viewModelScope.launch(Dispatchers.IO) { repository.deleteItem(current) }
    }
}

class LearningEditViewModel(private val repository: LearningRepository) : ViewModel() {
    suspend fun loadForEdit(id: Long): LearningItem? = repository.findById(id)

    fun save(item: LearningItem) {
        viewModelScope.launch(Dispatchers.IO) {
            if (item.id == 0L) {
                repository.insert(item)
            } else {
                repository.update(item)
            }
        }
    }
}

class DashboardViewModel(private val repository: LearningRepository) : ViewModel() {
    val summary = repository.observeSummary().asLiveData()
    val nextUp = repository.observeNextUp().asLiveData()
}

class BrowseViewModel(private val repository: LearningRepository) : ViewModel() {
    private val suggestions = MutableStateFlow(defaultSuggestions())

    private val _events = MutableSharedFlow<BrowseEvent>()
    val events = _events.asSharedFlow()

    val browseItems = combine(suggestions, repository.observeItems()) { list, existing ->
        val urls = existing.map { it.url }.toSet()
        list.map { suggestion ->
            BrowseUiModel(suggestion = suggestion, alreadyAdded = urls.contains(suggestion.url))
        }
    }.asLiveData()

    fun addToLibrary(suggestion: BrowseSuggestion) {
        viewModelScope.launch(Dispatchers.IO) {
            val exists = repository.existsByUrl(suggestion.url)
            if (exists) {
                _events.emit(BrowseEvent.AlreadyInLibrary(suggestion.title))
            } else {
                repository.insert(
                    LearningItem(
                        title = suggestion.title,
                        url = suggestion.url,
                        source = suggestion.source,
                        tags = suggestion.tags,
                        status = LearningStatus.TODO,
                        note = "",
                        addedAt = System.currentTimeMillis(),
                        completedAt = null
                    )
                )
                _events.emit(BrowseEvent.AddedToLibrary(suggestion.title))
            }
        }
    }
}

data class BrowseUiModel(
    val suggestion: BrowseSuggestion,
    val alreadyAdded: Boolean
)

sealed interface BrowseEvent {
    val title: String

    data class AddedToLibrary(override val title: String) : BrowseEvent
    data class AlreadyInLibrary(override val title: String) : BrowseEvent
}

private fun defaultSuggestions(): List<BrowseSuggestion> = listOf(
    BrowseSuggestion(
        title = "Designing Your Learning Roadmap",
        source = "FutureLearn",
        url = "https://www.futurelearn.com/info/blog/design-a-learning-plan",
        description = "Create a long-term learning strategy with checkpoints and reflection prompts.",
        tags = listOf("Planning", "Self-Improvement"),
        duration = "15 min read"
    ),
    BrowseSuggestion(
        title = "Building a Kotlin Coroutines Mental Model",
        source = "Kotlinlang",
        url = "https://kotlinlang.org/docs/coroutines-guide.html",
        description = "Understand structured concurrency, scopes, and flows in modern Kotlin.",
        tags = listOf("Kotlin", "Concurrency"),
        duration = "2 hr course"
    ),
    BrowseSuggestion(
        title = "Deep Work for Developers",
        source = "Medium",
        url = "https://medium.com/swlh/deep-work-for-developers-4aefb1b72a6",
        description = "Tactics to reclaim focus time and ship meaningful projects.",
        tags = listOf("Productivity", "Mindset"),
        duration = "10 min read"
    ),
    BrowseSuggestion(
        title = "Product Analytics Crash Course",
        source = "Amplitude",
        url = "https://academy.amplitude.com/path/product-analytics-crash-course",
        description = "Learn funnels, cohorts, and retention with practical product metrics exercises.",
        tags = listOf("Analytics", "Product"),
        duration = "1.5 hr course"
    ),
    BrowseSuggestion(
        title = "Navigate the Research Rabbit Hole",
        source = "Ness Labs",
        url = "https://nesslabs.com/research-rabbit-hole",
        description = "A framework for exploring new topics without losing momentum.",
        tags = listOf("Research", "Frameworks"),
        duration = "8 min read"
    ),
    BrowseSuggestion(
        title = "Systems Thinking 101",
        source = "Coursera",
        url = "https://www.coursera.org/learn/systems-thinking",
        description = "Recognize feedback loops, leverage points, and map complex systems.",
        tags = listOf("Systems", "Strategy"),
        duration = "4 week course"
    )
)

class ThemeViewModel(private val preferences: ThemePreferences) : ViewModel() {
    val themeMode = preferences.themeMode.asLiveData()

    fun toggleNightMode() {
        viewModelScope.launch {
            val current = preferences.themeMode.first()
            val next = when (current) {
                AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_NO
                AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_YES
            }
            preferences.setThemeMode(next)
        }
    }

    fun setMode(mode: Int) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }
}
