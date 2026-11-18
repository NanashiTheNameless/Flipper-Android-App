package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.local.presentation.decompose

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrKeyIdentifier
import dev.namelessnanashi.flipperzero.ifrmvp.model.PagesLayout
import dev.namelessnanashi.flipperzero.infrared.api.InfraredConnectionApi.InfraredEmulateState
import dev.namelessnanashi.flipperzero.infrared.editor.core.model.InfraredRemote
import dev.namelessnanashi.flipperzero.remotecontrols.api.FlipperDispatchDialogApi
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface LocalGridComponent {
    fun model(coroutineScope: CoroutineScope): StateFlow<Model>
    fun onButtonClick(identifier: IfrKeyIdentifier)
    fun onButtonLongClick(identifier: IfrKeyIdentifier)
    fun onButtonRelease()
    fun onRename(onEndAction: (FlipperKeyPath) -> Unit)
    fun onDelete(onEndAction: () -> Unit)
    fun toggleFavorite()
    fun pop()
    fun dismissDialog()

    sealed interface Model {
        data object Loading : Model
        data class Loaded(
            val pagesLayout: PagesLayout,
            val remotes: ImmutableList<InfraredRemote>,
            val flipperDialog: FlipperDispatchDialogApi.DialogType? = null,
            val emulatedKey: IfrKeyIdentifier?,
            val connectionState: InfraredEmulateState,
            val keyPath: FlipperKeyPath,
            val isFavorite: Boolean
        ) : Model {
            val isSynchronizing = listOf(
                InfraredEmulateState.SYNCING,
                InfraredEmulateState.CONNECTING
            ).contains(connectionState)
            val isConnected = connectionState != InfraredEmulateState.NOT_CONNECTED
        }

        data object Error : Model
    }

    fun interface Factory {
        fun invoke(
            componentContext: ComponentContext,
            keyPath: FlipperKeyPath,
            onBack: DecomposeOnBackParameter,
        ): LocalGridComponent
    }
}
