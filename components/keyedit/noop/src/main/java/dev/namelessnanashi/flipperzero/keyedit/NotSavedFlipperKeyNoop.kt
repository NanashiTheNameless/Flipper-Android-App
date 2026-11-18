package dev.namelessnanashi.flipperzero.keyedit

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperFile
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperKeyApi
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<NotSavedFlipperKeyApi>())
class NotSavedFlipperKeyNoop @Inject constructor() : NotSavedFlipperKeyApi {
    override suspend fun toNotSavedFlipperFile(flipperFile: FlipperFile): NotSavedFlipperFile {
        throw NotImplementedError()
    }
}
