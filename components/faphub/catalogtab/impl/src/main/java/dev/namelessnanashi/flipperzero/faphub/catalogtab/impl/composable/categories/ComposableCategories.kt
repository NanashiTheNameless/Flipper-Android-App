package dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.composable.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.model.CategoriesLoadState
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory

private const val DEFAULT_CATEGORIES_SIZE = 12
private const val COLUMN_COUNT = 3

@Suppress("FunctionNaming")
fun LazyListScope.ComposableCategories(
    loadState: CategoriesLoadState,
    onCategoryClick: (FapCategory) -> Unit,
) {
    when (loadState) {
        is CategoriesLoadState.Loaded -> ComposableCategoriesGridItems(
            loadState.categories,
            onClick = onCategoryClick
        )

        CategoriesLoadState.Loading -> ComposableCategoriesGridItems(
            MutableList<FapCategory?>(
                DEFAULT_CATEGORIES_SIZE
            ) { null },
            onClick = onCategoryClick
        )

        is CategoriesLoadState.Error -> Unit
    }
}

@Suppress("FunctionNaming")
private fun LazyListScope.ComposableCategoriesGridItems(
    categories: List<FapCategory?>,
    onClick: (FapCategory) -> Unit
) = gridItems(
    data = categories,
    columnCount = COLUMN_COUNT,
    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
) {
    ComposableCategoryCard(
        modifier = if (it != null) {
            Modifier.clickableRipple { onClick(it) }
        } else {
            Modifier
        },
        fapCategory = it
    )
}
