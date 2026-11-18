package dev.namelessnanashi.flipperzero.info.impl.viewmodel.deviceinfo

import android.app.Application
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperRpcInformation
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.StorageStats
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.flashIntStats
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.flashSdStats
import dev.namelessnanashi.flipperzero.core.ktx.jre.createClearNewFileWithMkDirs
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.share.SharableFile
import dev.namelessnanashi.flipperzero.core.share.ShareHelper
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.info.impl.R
import dev.namelessnanashi.flipperzero.info.impl.model.FlipperBasicInfo
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import dev.zacsweers.metro.Inject

class ShareFullInfoFileViewModel @Inject constructor(
    private val application: Application,
) : DecomposeViewModel() {
    fun shareDeviceInfo(
        flipperRpcInformation: FlipperRpcInformation?,
        basicInfo: FlipperBasicInfo
    ) {
        val internalStorageStats = basicInfo.storageInfo.flashIntStats
        val externalStorageStats = basicInfo.storageInfo.flashSdStats
        if (flipperRpcInformation == null ||
            internalStorageStats == null ||
            externalStorageStats == null
        ) {
            return
        }

        val file = SharableFile(nameFile = getFileName(flipperRpcInformation), context = application)
        file.createClearNewFileWithMkDirs()

        try {
            addInfoToFile(flipperRpcInformation, internalStorageStats, externalStorageStats, file)
            ShareHelper.shareFile(
                context = application,
                file = file,
                resId = R.string.device_info_share
            )
        } catch (@Suppress("SwallowedException") exception: Exception) {
            error(exception) { "Exception when upload device info: $exception" }
        }
    }

    private fun getFileName(flipperRpcInformation: FlipperRpcInformation): String {
        val flipperName = flipperRpcInformation.flipperDeviceInfo.deviceName ?: "unknown"
        val formatDate = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
        )
        return "dump-$flipperName-$formatDate.txt"
    }

    private fun addInfoToFile(
        flipperRpcInformation: FlipperRpcInformation,
        internalStorageStats: StorageStats,
        externalStorageStats: StorageStats,
        file: File
    ) {
        val builder = StringBuilder()
        flipperRpcInformation.allFields.forEach { (key, value) ->
            builder.appendLine("$key: $value")
        }
        if (internalStorageStats is StorageStats.Loaded) {
            builder.appendLine("int_available: ${internalStorageStats.free}")
            builder.appendLine("int_total: ${internalStorageStats.total}")
        }

        if (externalStorageStats is StorageStats.Loaded) {
            builder.appendLine("ext_available: ${externalStorageStats.free}")
            builder.appendLine("ext_total: ${externalStorageStats.total}")
        }

        file.appendText(builder.toString())
    }
}
