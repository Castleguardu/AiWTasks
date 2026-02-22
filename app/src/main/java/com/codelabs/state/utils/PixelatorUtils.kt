package com.codelabs.state.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build

object PixelatorUtils {

    /**
     * 将 Bitmap 转换为像素风
     * 原理：先缩小 (丢弃细节)，再放大 (保持锯齿)。
     *
     * @param inputBitmap 原始位图
     * @param targetSize 目标像素大小 (例如 64x64 的格子)
     * @return 像素化后的 Bitmap (尺寸通常会变小，显示时由 UI 拉伸，或者这里直接拉伸回原大小)
     */
    fun pixelate(inputBitmap: Bitmap, targetSize: Int = 64): Bitmap {
        // 0. 确保输入 Bitmap 是 Software Config (非 HARDWARE)，否则 Canvas 绘制会崩溃
        val bitmap = if (inputBitmap.config == Bitmap.Config.HARDWARE) {
            inputBitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            inputBitmap
        }

        // 1. 计算缩放比例，保持长宽比
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val width: Int
        val height: Int
        if (bitmap.width > bitmap.height) {
            width = targetSize
            height = (targetSize / aspectRatio).toInt()
        } else {
            height = targetSize
            width = (targetSize * aspectRatio).toInt()
        }

        // 2. 缩小图片 (Downscale)
        // 使用 filter = true (默认) 也可以，但 false 更硬朗。
        // 这里的关键是缩小动作本身就会丢失信息。
        val tinyBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

        // 3. (可选) 如果需要返回一张大图以便 UI 方便显示，我们可以再放大回去
        // 关键点：放大时必须使用 Nearest Neighbor 插值 (filter = false)
        // 这里的 outputSize 可以是原图大小，或者一个固定的显示大小 (如 512)
        // 为了存储节省空间，我们只返回小的 tinyBitmap，让 UI (Coil) 去负责无模糊放大显示。
        // 但考虑到兼容性，我们这里返回一个适中大小的像素图 (例如 256px)，方便存储和查看。
        
        val displaySize = 256
        val displayWidth: Int
        val displayHeight: Int
        if (width > height) {
            displayWidth = displaySize
            displayHeight = (displaySize / aspectRatio).toInt()
        } else {
            displayHeight = displaySize
            displayWidth = (displaySize * aspectRatio).toInt()
        }

        val pixelatedBitmap = Bitmap.createBitmap(displayWidth, displayHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(pixelatedBitmap)
        val paint = Paint().apply {
            isAntiAlias = false
            isFilterBitmap = false // 核心：关闭滤波，使用邻近插值
            isDither = false
        }
        
        // 将小图绘制到大图上
        canvas.drawBitmap(tinyBitmap, null, android.graphics.Rect(0, 0, displayWidth, displayHeight), paint)
        
        // 回收中间变量
        if (tinyBitmap != bitmap) {
            tinyBitmap.recycle()
        }
        // 如果 inputBitmap 是 Hardware 的，我们也 copy 了一份 bitmap，这里应该回收 copy 的份
        if (bitmap != inputBitmap) {
            bitmap.recycle()
        }
        
        return pixelatedBitmap
    }
}
