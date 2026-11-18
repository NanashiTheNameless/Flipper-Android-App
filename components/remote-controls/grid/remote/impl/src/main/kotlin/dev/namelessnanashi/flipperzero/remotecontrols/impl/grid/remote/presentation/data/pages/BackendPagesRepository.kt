package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.data.pages

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.ifrmvp.api.infrared.FlipperInfraredBackendApi
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.toPagesLayout
import dev.namelessnanashi.flipperzero.ifrmvp.model.PagesLayout
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<PagesRepository>())
class BackendPagesRepository @Inject constructor(
    private val infraredBackendApi: FlipperInfraredBackendApi,
) : PagesRepository {
    override suspend fun fetchDefaultPageLayout(
        ifrFileId: Long
    ): Result<PagesLayout> = runCatching<PagesLayout> {
        infraredBackendApi.getUiFile(ifrFileId).toPagesLayout()
    }

    override suspend fun fetchKeyContent(
        ifrFileId: Long
    ): Result<String> = runCatching {
        infraredBackendApi.getIfrFileContent(ifrFileId).content
    }
}
