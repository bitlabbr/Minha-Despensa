/*
 *   Copyright (c) 2026 Willian Santos
 *
 *   This work is licensed under the Creative Commons
 *   Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 *
 *   You are free to:
 *     - Share  — copy and redistribute the material in any medium or format
 *     - Adapt  — remix, transform, and build upon the material
 *
 *   Under the following terms:
 *     - Attribution    — You must give appropriate credit, provide a link to
 *                        the license, and indicate if changes were made.
 *     - NonCommercial  — You may not use the material for commercial purposes.
 *
 *   Owner rights:
 *     - Willian Santos retains all commercial rights.
 *    - The copyright holder may use, sell, sublicense, or relicense this
 *       work under different terms at any time.
 *
 *   Full license: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package com.bitlabbr.minhadespensa.app.provider

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import com.bitlabbr.minhadespensa.core.domain.util.DiagnosticLogRecorder
import com.bitlabbr.minhadespensa.data.util.SupportPlatformProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidSupportPlatformProvider(
    private val context: Context,
) : SupportPlatformProvider {

    private val crashFile: File
        get() = File(context.filesDir, "pending_crash.txt")

    override fun getDeviceModel(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) model else "$manufacturer $model"
    }

    override fun getOsVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    override fun getAppVersionName(): String {
        return runCatching {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        }.getOrDefault("1.0.0")
    }

    override suspend fun savePendingCrash(stackTrace: String) = withContext(Dispatchers.IO) {
        runCatching {
            crashFile.writeText(stackTrace)
        }.getOrDefault(Unit)
    }

    override suspend fun getPendingCrash(): String? = withContext(Dispatchers.IO) {
        runCatching {
            if (crashFile.exists() && crashFile.length() > 0) {
                crashFile.readText()
            } else {
                null
            }
        }.getOrNull()
    }

    override suspend fun clearPendingCrash() = withContext(Dispatchers.IO) {
        runCatching {
            if (crashFile.exists()) {
                crashFile.delete()
            }
        }.getOrDefault(Unit)
    }

    override suspend fun shareLogFile(title: String, content: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val logsDir = File(context.cacheDir, "logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            val reportFile = File(logsDir, "diagnostic_report.txt")
            reportFile.writeText(content)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                reportFile,
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Segue anexo o arquivo de diagnóstico e logs do aplicativo Minha Despensa.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(sendIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    fun installCrashHandler(recorder: DiagnosticLogRecorder? = null) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val stackTrace = throwable.stackTraceToString()
                recorder?.record("FATAL EXCEPTION on thread ${thread.name}:\n$stackTrace")
                crashFile.writeText(stackTrace)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
