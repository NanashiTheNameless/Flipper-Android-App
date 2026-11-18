package dev.namelessnanashi.flipperzero.filemanager.create.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileUploadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.FileType
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.ListingItem
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.StorageRequestPriority
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperFileNameValidator
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.filemanager.util.constant.FileManagerConstants
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.ByteString
import okio.Path
import okio.buffer
import dev.zacsweers.metro.Inject

class CreateFileViewModel @Inject constructor(
    private val featureProvider: FFeatureProvider,
) : DecomposeViewModel(), LogTagProvider {
    override val TAG: String = "CreateFileViewModel"

    private val fileNameValidator = FlipperFileNameValidator()

    private val eventChannel = Channel<Event>()
    val event = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow<State>(State.Pending)
    val state = _state.asStateFlow()

    private var featureJob: Job? = null
    private val featureMutex = Mutex()

    val canCreateFiles = featureProvider.get<FStorageFeatureApi>()
        .filterIsInstance<FFeatureStatus.Supported<FStorageFeatureApi>>()
        .map { true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun startCreate(parent: Path, type: FileType) {
        _state.update {
            State.Creating(
                parent = parent,
                type = type,
                name = "",
                isValid = false,
                isCreating = false
            )
        }
    }

    fun startCreateFile(parent: Path) {
        startCreate(parent, FileType.FILE)
    }

    fun startCreateFolder(parent: Path) {
        startCreate(parent, FileType.DIR)
    }

    fun dismiss() {
        _state.update { State.Pending }
    }

    fun onNameChange(name: String) {
        _state.update { state ->
            (state as? State.Creating)
                ?.copy(name = name)
                ?: state
        }
    }

    fun onOptionSelected(index: Int) {
        _state.update { state ->
            (state as? State.Creating)?.let { renamingState ->
                val option = renamingState.options.getOrNull(index).orEmpty()
                renamingState.copy(name = "${renamingState.name}.$option")
            } ?: state
        }
    }

    private suspend fun FFileUploadApi.createNewFile(pathOnFlipper: String): Result<Unit> {
        return runCatching {
            sink(
                pathOnFlipper = pathOnFlipper,
                priority = StorageRequestPriority.FOREGROUND
            ).buffer().use { bufferedSink -> bufferedSink.write(ByteString.of()) }
        }
    }

    private suspend fun create(uploadApi: FFileUploadApi) {
        val state = (state.first() as? State.Creating)
        if (state == null) {
            error { "#rename state was not Renaming" }
            return
        }
        val fullPath = state.parent.resolve(state.name)
        _state.emit(state.copy(isCreating = true))
        when (state.type) {
            FileType.FILE -> {
                uploadApi.createNewFile(fullPath.toString())
            }

            FileType.DIR -> {
                uploadApi.mkdir(fullPath.toString())
            }
        }.onSuccess {
            val item = ListingItem(
                fileType = state.type,
                fileName = fullPath.name,
                size = 0L
            )
            val event = Event.Created(item)
            eventChannel.send(event)
        }.onFailure {
            error(it) { "Could not create file $fullPath" }
        }
        _state.emit(State.Pending)
        featureJob?.cancelAndJoin()
    }

    fun onConfirm() {
        viewModelScope.launch {
            featureJob?.cancelAndJoin()
            featureMutex.withLock {
                featureJob = featureProvider.get<FStorageFeatureApi>()
                    .onEach { status ->
                        when (status) {
                            FFeatureStatus.NotFound -> Unit
                            FFeatureStatus.Retrieving -> Unit
                            FFeatureStatus.Unsupported -> Unit
                            is FFeatureStatus.Supported -> {
                                create(status.featureApi.uploadApi())
                            }
                        }
                    }.launchIn(viewModelScope)
                featureJob?.join()
            }
        }
    }

    private fun collectNameValidation() {
        state
            .filterIsInstance<State.Creating>()
            .distinctUntilChangedBy { state -> state.name }
            .onEach { state ->
                _state.emit(state.copy(isValid = fileNameValidator.isValid(state.name)))
            }.launchIn(viewModelScope)
    }

    init {
        collectNameValidation()
    }

    sealed interface State {
        data object Pending : State
        data class Creating(
            val parent: Path,
            val name: String,
            val isValid: Boolean,
            val type: FileType,
            val isCreating: Boolean
        ) : State {
            val options: ImmutableList<String>
                get() = when (type) {
                    FileType.FILE -> FileManagerConstants.FILE_EXTENSION_HINTS

                    FileType.DIR -> emptyList()
                }.toImmutableList()

            val needShowOptions: Boolean
                get() = !name.contains(".") && options.isNotEmpty()
        }
    }

    sealed interface Event {
        data class Created(val item: ListingItem) : Event
    }
}
