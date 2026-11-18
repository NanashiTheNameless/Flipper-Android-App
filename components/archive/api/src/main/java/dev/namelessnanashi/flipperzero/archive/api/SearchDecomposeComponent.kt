package dev.namelessnanashi.flipperzero.archive.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.ui.decompose.CompositeDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter

abstract class SearchDecomposeComponent<C : Any> : CompositeDecomposeComponent<C>() {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onItemSelected: SelectKeyPathListener?,
            onBack: DecomposeOnBackParameter
        ): SearchDecomposeComponent<*>
    }
}

fun interface SelectKeyPathListener {
    operator fun invoke(keyPath: FlipperKeyPath)
}
