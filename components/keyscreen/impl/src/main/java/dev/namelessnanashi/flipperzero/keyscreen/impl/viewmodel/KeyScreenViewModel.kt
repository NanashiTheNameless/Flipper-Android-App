package dev.namelessnanashi.flipperzero.keyscreen.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.keyscreen.api.KeyStateHelperApi
import dev.namelessnanashi.flipperzero.keyscreen.model.KeyScreenState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.StateFlow

@Suppress("LongParameterList")
class KeyScreenViewModel @AssistedInject constructor(
    @Assisted val paramKeyPath: FlipperKeyPath, // For get value to bottom sheet
    keyStateHelperApi: KeyStateHelperApi.Builder,
) : DecomposeViewModel(), LogTagProvider {
    override val TAG = "KeyScreenViewModel"

    private val keyStateHelper = keyStateHelperApi.build(paramKeyPath, viewModelScope)

    fun getKeyPath(): FlipperKeyPath {
        return (keyStateHelper.getKeyScreenState().value as? KeyScreenState.Ready)
            ?.flipperKey
            ?.getKeyPath()
            ?: paramKeyPath
    }

    fun getKeyScreenState(): StateFlow<KeyScreenState> = keyStateHelper.getKeyScreenState()

    fun setFavorite(isFavorite: Boolean) = keyStateHelper.setFavorite(isFavorite)

    fun onOpenEdit(onEndAction: (FlipperKeyPath) -> Unit) = keyStateHelper.onOpenEdit(onEndAction)

    fun onDelete(onEndAction: () -> Unit) = keyStateHelper.onDelete(onEndAction)

    fun onRestore(onEndAction: () -> Unit) = keyStateHelper.onRestore(onEndAction)

    fun openNfcEditor(onEndAction: (FlipperKeyPath) -> Unit) {
        val state = getKeyScreenState().value
        if (state !is KeyScreenState.Ready) return

        val flipperKey = state.flipperKey
        val flipperKeyPath = flipperKey.getKeyPath()
        onEndAction(flipperKeyPath)
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(paramKeyPath: FlipperKeyPath): KeyScreenViewModel
    }
}
