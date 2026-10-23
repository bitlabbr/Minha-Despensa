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

package com.bitlabbr.minhadespensa.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.FileProvider
import com.bitlabbr.minhadespensa.data.util.AppPackageInstaller
import com.bitlabbr.minhadespensa.data.util.AppPlatformInfoProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidPlatformInfoProvider(
    private val context: Context,
) : AppPlatformInfoProvider {

    override fun isSelfUpdateSupported(): Boolean = true

    override fun getPlatformName(): String = "android"

    override fun getVersionName(): String {
        return runCatching {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        }.getOrDefault("1.0.0")
    }

    override fun getVersionCode(): Long {
        return runCatching {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
        }.getOrDefault(1000L)
    }
}

class AndroidPackageInstaller(
    private val context: Context,
) : AppPackageInstaller {

    override suspend fun openApkFile(
        bytesProvider: suspend (onChunk: suspend (ByteArray) -> Unit) -> Unit,
        fileName: String,
    ): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val updatesDir = File(context.cacheDir, "updates")
            if (!updatesDir.exists()) {
                updatesDir.mkdirs()
            }
            val apkFile = File(updatesDir, fileName)
            if (apkFile.exists()) {
                apkFile.delete()
            }

            FileOutputStream(apkFile).use { fos ->
                bytesProvider { chunk ->
                    fos.write(chunk)
                }
                fos.flush()
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile,
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
        }
    }
}
