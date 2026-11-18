package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.data.pages

import dev.namelessnanashi.flipperzero.ifrmvp.model.PagesLayout

interface PagesRepository {
    suspend fun fetchDefaultPageLayout(ifrFileId: Long): Result<PagesLayout>
    suspend fun fetchKeyContent(ifrFileId: Long): Result<String>
}
