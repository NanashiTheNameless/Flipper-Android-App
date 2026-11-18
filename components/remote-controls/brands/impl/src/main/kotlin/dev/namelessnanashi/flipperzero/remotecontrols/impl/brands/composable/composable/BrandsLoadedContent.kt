package dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.composable.composable

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.BrandModel
import dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.composable.composable.alphabet.AlphabetSearchComposable
import dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.presentation.decompose.BrandsDecomposeComponent
import dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.presentation.util.charSection

@Composable
fun BrandsLoadedContent(
    model: BrandsDecomposeComponent.Model.Loaded,
    onBrandClick: (BrandModel) -> Unit,
    onBrandLongClick: (BrandModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    AlphabetSearchComposable(
        modifier = modifier,
        items = model.sortedBrands,
        toHeader = { it.name.first().uppercaseChar() },
        headers = model.headers,
        listState = listState,
        content = {
            ItemsList(
                modifier = Modifier.weight(1f),
                listState = listState,
                items = model.sortedBrands,
                onClick = onBrandClick,
                onLongClick = onBrandLongClick,
                toString = { it.name },
                toCharSection = { it.charSection() }
            )
        }
    )
}
