package dev.namelessnanashi.flipperzero.keyedit.impl.viewmodel.processors

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.keyedit.impl.model.EditableKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Binds

@ContributesTo(AppGraph::class)
interface EditableKeyProcessorModule {
    @Binds
    fun provideExistedKeyProcessor(
        existedKeyProcessor: ExistedKeyProcessor
    ): EditableKeyProcessor<EditableKey.Existed>

    @Binds
    fun provideLimbKeyProcessor(
        limboKeyProcessor: LimboKeyProcessor
    ): EditableKeyProcessor<EditableKey.Limb>
}
