package dev.namelessnanashi.flipperzero.ifrmvp.backend.model

import dev.namelessnanashi.flipperzero.ifrmvp.model.PageLayout
import dev.namelessnanashi.flipperzero.ifrmvp.model.PagesLayout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Copy of [PagesLayout] from core-model module
 * @see dev.namelessnanashi.flipperzero.ifrmvp.model.PagesLayout
 *
 * Workaround for https://github.com/Foso/Ktorfit/issues/594
 */
@Serializable
class PagesLayoutBackendModel(
    @SerialName("pages")
    val pages: List<PageLayout>
)

fun PagesLayoutBackendModel.toPagesLayout() = PagesLayout(pages)
