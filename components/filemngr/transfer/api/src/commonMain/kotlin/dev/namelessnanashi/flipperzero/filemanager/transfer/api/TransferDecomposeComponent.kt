package dev.namelessnanashi.flipperzero.filemanager.transfer.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.filemanager.transfer.api.model.TransferType
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import okio.Path

abstract class TransferDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            param: Param,
            onBack: DecomposeOnBackParameter,
            onMoved: MovedCallback,
            onPathChange: PathChangedCallback,
        ): TransferDecomposeComponent
    }

    fun interface MovedCallback {
        fun invoke(path: Path)
    }

    fun interface PathChangedCallback {
        fun invoke(path: Path)
    }

    data class Param(
        val path: Path,
        val transferType: TransferType,
        val fullPathToMove: List<Path>,
    )
}
