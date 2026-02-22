package com.codelabs.state.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    repository: TaskRepository,
    private val avatarManager: AvatarManager,
    private val applicationContext: Context // 需要 Context 来解析 Uri，虽然通常建议不传，但解析 Uri 必须 contentResolver
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

    // 头像状态：File 对象
    private val _avatarFile = MutableStateFlow<File?>(null)
    val avatarFile: StateFlow<File?> = _avatarFile.asStateFlow()

    init {
        // 初始化加载头像
        viewModelScope.launch {
            _avatarFile.value = avatarManager.getAvatarFile()
        }
    }

    /**
     * 处理用户选择的图片 Uri
     */
    fun onAvatarSelected(uri: Uri) {
        viewModelScope.launch {
            val originalBitmap = loadBitmapFromUri(uri)
            if (originalBitmap != null) {
                // 1. 像素化
                val pixelBitmap = PixelatorUtils.pixelate(originalBitmap)
                
                // 2. 保存
                avatarManager.saveAvatar(pixelBitmap)
                
                // 3. 刷新状态 (强制触发 StateFlow 更新，因为 File 对象引用可能没变但内容变了，这里我们新建一个 File 对象引用或者使用时间戳)
                // 简单起见，重新获取
                _avatarFile.value = avatarManager.getAvatarFile()
            }
        }
    }

    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(applicationContext.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
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
