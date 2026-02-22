package com.codelabs.state.data.source

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AvatarManager(private val context: Context) {

    private val avatarFile = File(context.filesDir, "avatar.png")

    suspend fun saveAvatar(bitmap: Bitmap) = withContext(Dispatchers.IO) {
        FileOutputStream(avatarFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    fun getAvatarFile(): File? {
        return if (avatarFile.exists()) avatarFile else null
    }
    
    // 如果需要给外部应用分享，可能需要 FileProvider，但这里 UI 显示直接用 File 对象即可。
}
