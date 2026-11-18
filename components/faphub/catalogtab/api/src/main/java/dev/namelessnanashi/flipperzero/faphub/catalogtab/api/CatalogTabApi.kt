package dev.namelessnanashi.flipperzero.faphub.catalogtab.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort

@Immutable
interface CatalogTabApi {
    @Composable
    @Suppress("NonSkippableComposable")
    fun ComposableCatalogTab(
        componentContext: ComponentContext,
        onOpenFapItem: (FapItemShort) -> Unit,
        onCategoryClick: (FapCategory) -> Unit
    )
}
