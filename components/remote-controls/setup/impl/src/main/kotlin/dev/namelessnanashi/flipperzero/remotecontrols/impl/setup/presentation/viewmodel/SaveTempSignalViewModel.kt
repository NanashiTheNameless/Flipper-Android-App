package dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.presentation.viewmodel

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.core.FlipperStorageProvider
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.launchWithLock
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.remotecontrols.api.SaveTempSignalApi
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

private const val EXT_PATH = "/ext"

@ContributesBinding(AppGraph::class, binding<SaveTempSignalApi>())
class SaveTempSignalViewModel @Inject constructor(
    private val fFeatureProvider: FFeatureProvider,
    private val storageProvider: FlipperStorageProvider
) : DecomposeViewModel(),
    LogTagProvider,
    SaveTempSignalApi {
    private val mutex = Mutex()
    override val TAG: String = "SaveFileViewModel"
    private val _state = MutableStateFlow<SaveTempSignalApi.State>(SaveTempSignalApi.State.Pending)
    override val state = _state.asStateFlow()

    override fun saveFiles(
        vararg filesDesc: SaveTempSignalApi.FileDesc,
        onFinished: () -> Unit,
    ) {
        viewModelScope.launch {
            val fStorageFeatureApi = fFeatureProvider.getSync<FStorageFeatureApi>() ?: run {
                error { "#saveFiles could not get FStorageFeatureApi" }
                return@launch
            }
            _state.emit(SaveTempSignalApi.State.Uploading(0f))
            launchWithLock(mutex, viewModelScope, "load") {
                filesDesc.forEachIndexed { _, fileDesc ->
                    storageProvider.useTemporaryFile { deviceFile ->
                        deviceFile.toFile().writeText(fileDesc.textContent)
                        val fAbsolutePath = FlipperFilePath(
                            folder = fileDesc.extFolderPath,
                            nameWithExtension = fileDesc.nameWithExtension
                        ).getPathOnFlipper()
                        fStorageFeatureApi.uploadApi().mkdir("$EXT_PATH/${fileDesc.extFolderPath}")
                        fStorageFeatureApi.uploadApi().upload(
                            pathOnFlipper = fAbsolutePath,
                            fileOnAndroid = deviceFile,
                            progressListener = { current, max ->
                                _state.value = SaveTempSignalApi.State.Uploading(
                                    progressPercent = if (max == 0L) 0f else current / max.toFloat()
                                )
                            }
                        )
                    }
                }
                _state.value = SaveTempSignalApi.State.Uploaded
                onFinished.invoke()
            }
        }
    }
}
