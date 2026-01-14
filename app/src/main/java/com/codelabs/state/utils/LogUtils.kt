package com.codelabs.state.utils

import android.util.Log
import com.codelabs.state.BuildConfig

object LogUtils {
    private const val GLOBAL_PREFIX = "wTask"
    
    // 依然保留这个标记，但只用来控制低优先级的日志
    private val IS_DEBUG = BuildConfig.DEBUG

    private fun genTag(tag: String): String {
        return "$GLOBAL_PREFIX-$tag"
    }

    /**
     * Debug 级别：通常包含详细的流程、JSON数据等。
     * 策略：【仅在 Debug 模式下打印】
     * 原因：量大、影响性能、容易泄露敏感数据。
     */
    fun d(tag: String, msg: String) {
        if (IS_DEBUG) {
            Log.d(genTag(tag), msg)
        }
    }

    /**
     * Info 级别：关键流程节点（如：进入页面、点击按钮、开始请求）。
     * 策略：【始终打印】
     */
    fun i(tag: String, msg: String) {
        Log.i(genTag(tag), msg)
    }

    /**
     * Warning 级别：不符合预期但程序还能跑的情况。
     * 策略：【始终打印】
     */
    fun w(tag: String, msg: String) {
        Log.w(genTag(tag), msg)
    }

    /**
     * Error 级别：程序崩溃、Try-Catch 捕获的异常。
     * 策略：【始终打印】
     * 建议：这里也是上传日志到服务器（如 Firebase Crashlytics）的最佳位置
     */
    fun e(tag: String, msg: String, tr: Throwable? = null) {
        val finalTag = genTag(tag)
        if (tr != null) {
            Log.e(finalTag, msg, tr)
        } else {
            Log.e(finalTag, msg)
        }

        // TODO: 如果接入了 Firebase Crashlytics 或 Sentry，可以在这里加代码
        // if (!IS_DEBUG) {
        //     FirebaseCrashlytics.getInstance().log("$finalTag: $msg")
        //     if (tr != null) FirebaseCrashlytics.getInstance().recordException(tr)
        // }
    }
}