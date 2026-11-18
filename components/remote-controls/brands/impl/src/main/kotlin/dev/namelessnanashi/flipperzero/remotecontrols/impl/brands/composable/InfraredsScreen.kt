package dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.composable

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.IfrFileModel
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.layout.shared.ErrorComposable
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.layout.shared.SharedTopBar
import dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.composable.composable.BrandsLoadingComposable
import dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.composable.composable.ItemsList
import dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.composable.composable.alphabet.AlphabetSearchComposable
import dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.presentation.viewmodel.InfraredsListViewModel
import kotlinx.collections.immutable.toImmutableSet
import dev.namelessnanashi.flipperzero.remotecontrols.brands.impl.R as BrandsR

@Composable
fun InfraredsScreen(
    viewModel: InfraredsListViewModel,
    onBack: () -> Unit,
    onReload: () -> Unit,
    onClick: (IfrFileModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        modifier = modifier,
        topBar = {
            SharedTopBar(
                title = stringResource(BrandsR.string.infrareds_title),
                subtitle = stringResource(BrandsR.string.brands_subtitle),
                onBackClick = onBack
            )
        }
    ) { scaffoldPaddings ->
        Crossfade(
            targetState = state,
            modifier = Modifier.padding(scaffoldPaddings)
        ) { model ->
            when (model) {
                InfraredsListViewModel.State.Error -> {
                    ErrorComposable(onReload = onReload)
                }

                is InfraredsListViewModel.State.Loaded -> {
                    val listState = rememberLazyListState()
                    AlphabetSearchComposable(
                        items = model.infrareds,
                        toHeader = { it.folderName.first().uppercaseChar() },
                        headers = remember(model) {
                            model.infrareds
                                .map { it.fileName.first().uppercaseChar() }
                                .toImmutableSet()
                        },
                        listState = listState,
                        content = {
                            ItemsList(
                                modifier = Modifier.weight(1f),
                                listState = listState,
                                items = model.infrareds,
                                onClick = onClick,
                                toCharSection = { it.folderName.first().uppercaseChar() },
                                toString = { it.folderName },
                                onLongClick = {}
                            )
                        }
                    )
                }

                InfraredsListViewModel.State.Loading -> {
                    BrandsLoadingComposable()
                }
            }
        }
    }
}
