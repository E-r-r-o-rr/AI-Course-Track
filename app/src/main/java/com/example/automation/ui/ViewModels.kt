package com.example.automation.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.automation.data.LearningRepository
import com.example.automation.data.preferences.ThemePreferences
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

    fun addToQueue(item: LearningItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addToQueue(item.id)
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

    fun saveChanges(note: String, status: LearningStatus) {
        val id = itemId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(id, note)
            repository.updateStatus(id, status)
        }
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
    val currentTasks = repository.observeCurrentTasks().asLiveData()
    val queuedItems = repository.observeQueuedItems().asLiveData()
    val completedItems = repository.observeCompletedItems().asLiveData()

    fun startItem(item: LearningItem) {
        viewModelScope.launch(Dispatchers.IO) { repository.startItem(item.id) }
    }

    fun removeFromQueue(item: LearningItem) {
        viewModelScope.launch(Dispatchers.IO) { repository.removeFromQueue(item.id) }
    }

    fun moveToQueue(item: LearningItem) {
        viewModelScope.launch(Dispatchers.IO) { repository.moveToQueue(item.id) }
    }

    fun completeItem(item: LearningItem) {
        viewModelScope.launch(Dispatchers.IO) { repository.completeItem(item.id) }
    }

    fun deleteItem(item: LearningItem) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteItem(item) }
    }
}

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
