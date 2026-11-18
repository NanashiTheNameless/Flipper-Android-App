package dev.namelessnanashi.flipperzero.archive.impl.composable.category

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.archive.impl.model.CategoryItem
import dev.namelessnanashi.flipperzero.archive.model.CategoryType
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ComposableCategoryCard(
    categories: ImmutableList<CategoryItem>,
    deletedCategory: CategoryItem,
    onOpenCategory: (CategoryType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(top = 14.dp, start = 14.dp, end = 14.dp, bottom = 2.dp)
    ) {
        ComposableCategoryList(onOpenCategory, categories, deletedCategory)
    }
}

@Composable
private fun ComposableCategoryList(
    onOpenCategory: (CategoryType) -> Unit,
    categories: ImmutableList<CategoryItem>,
    deletedCategory: CategoryItem,
) {
    Column {
        categories.forEach {
            ComposableCategoryItem(categoryItem = it, onOpenCategory = onOpenCategory)
        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = LocalPallet.current.divider12
        )

        ComposableCategoryItem(categoryItem = deletedCategory, onOpenCategory = onOpenCategory)
    }
}
