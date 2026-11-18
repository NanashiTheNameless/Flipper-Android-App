package dev.namelessnanashi.flipperzero.nfceditor.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.ui.decompose.CompositeDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter

abstract class NfcEditorDecomposeComponent<C : Any> : CompositeDecomposeComponent<C>() {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            flipperKeyPath: FlipperKeyPath,
            onBack: DecomposeOnBackParameter
        ): NfcEditorDecomposeComponent<*>
    }
}
