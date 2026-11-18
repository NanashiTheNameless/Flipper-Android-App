package dev.namelessnanashi.flipperzero.remotecontrols.api

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.model.EmulateConfig
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrKeyIdentifier
import dev.namelessnanashi.flipperzero.infrared.editor.core.model.InfraredRemote
import kotlinx.coroutines.flow.StateFlow

interface DispatchSignalApi : InstanceKeeper.Instance {
    val state: StateFlow<State>

    fun dismissBusyDialog()

    /**
     * Dispatch key from temporal file which contains only one key
     */
    fun dispatch(
        config: EmulateConfig,
        identifier: IfrKeyIdentifier,
        onDispatched: () -> Unit = {}
    )

    fun stopEmulate()

    fun reset()

    /**
     * Dispatch specific key from custom located remote
     */
    fun dispatch(
        identifier: IfrKeyIdentifier,
        isOneTime: Boolean = true,
        remotes: List<InfraredRemote>,
        ffPath: FlipperFilePath,
        onDispatched: () -> Unit = {}
    )

    sealed interface State {
        data object Pending : State
        data object FlipperIsBusy : State
        data object FlipperNotConnected : State
        data object FlipperNotSupported : State
        data class Emulating(val ifrKeyIdentifier: IfrKeyIdentifier) : State
        data object Error : State
    }
}
