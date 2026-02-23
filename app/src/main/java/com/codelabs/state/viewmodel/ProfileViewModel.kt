package com.codelabs.state.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codelabs.state.data.Milestone
import com.codelabs.state.data.UserStats
import com.codelabs.state.data.repository.TaskRepository
import com.codelabs.state.data.source.AvatarManager
import com.codelabs.state.utils.PixelatorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProfileViewModel(
    private val repository: TaskRepository,
    private val avatarManager: AvatarManager,
    private val applicationContext: Context
) : ViewModel() {

    val userStats: StateFlow<UserStats?> = repository.getUserStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val completedTasksCount: StateFlow<Int> = repository.getCompletedTasksCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // 里程碑列表
    val milestones: StateFlow<List<Milestone>> = repository.getAllMilestones()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 头像状态
    private val _avatarFile = MutableStateFlow<File?>(null)
    val avatarFile: StateFlow<File?> = _avatarFile.asStateFlow()

    init {
        viewModelScope.launch {
            _avatarFile.value = avatarManager.getAvatarFile()
        }
    }

    // --- 改名逻辑 ---
    fun updateUserName(newName: String) {
        viewModelScope.launch {
            repository.updateUserName(newName)
        }
    }

    // --- 羁绊逻辑 ---
    fun addMilestone(title: String, max: Int) {
        viewModelScope.launch {
            repository.addMilestone(title, max)
        }
    }

    fun incrementMilestoneProgress(milestone: Milestone) {
        if (milestone.currentProgress < milestone.maxProgress) {
            viewModelScope.launch {
                repository.updateMilestoneProgress(milestone.copy(currentProgress = milestone.currentProgress + 1))
            }
        }
    }

    // --- 头像逻辑 ---
    fun onAvatarSelected(uri: Uri) {
        viewModelScope.launch {
            val originalBitmap = loadBitmapFromUri(uri)
            if (originalBitmap != null) {
                val pixelBitmap = PixelatorUtils.pixelate(originalBitmap)
                avatarManager.saveAvatar(pixelBitmap)
                _avatarFile.value = avatarManager.getAvatarFile()
            }
        }
    }

    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(applicationContext.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE // 强制软件位图，防止 Pixelator 崩溃
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    class Factory(
        private val repository: TaskRepository,
        private val avatarManager: AvatarManager,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(repository, avatarManager, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
