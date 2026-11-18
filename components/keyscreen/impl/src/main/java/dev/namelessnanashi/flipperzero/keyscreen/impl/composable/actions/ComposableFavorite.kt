package dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.ktx.image.painterResourceByKey
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.keyscreen.model.FavoriteState
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

private const val ICON_SIZE_DP = 24

@Composable
fun ComposableFavorite(
    favoriteState: FavoriteState,
    modifier: Modifier = Modifier,
    onSwitchFavorites: (Boolean) -> Unit
) {
    val iconId = when (favoriteState) {
        FavoriteState.PROGRESS -> {
            CircularProgressIndicator(modifier = Modifier.size(ICON_SIZE_DP.dp))
            return
        }
        FavoriteState.FAVORITE -> DesignSystem.drawable.ic_star_enabled
        FavoriteState.NOT_FAVORITE -> DesignSystem.drawable.ic_star_disabled
    }

    Icon(
        modifier = modifier
            .clickableRipple(bounded = false) {
                // Inverse state
                onSwitchFavorites(favoriteState != FavoriteState.FAVORITE)
            }
            .size(ICON_SIZE_DP.dp),
        painter = painterResourceByKey(iconId),
        tint = LocalPallet.current.keyFavorite,
        contentDescription = null
    )
}
