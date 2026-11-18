package dev.namelessnanashi.flipperzero.updater.card.composable

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.updater.card.R
import dev.namelessnanashi.flipperzero.updater.card.model.UpdatePending
import dev.namelessnanashi.flipperzero.updater.model.UpdateCardState

@Composable
fun ComposableUpdateButtonContentChooseFile(
    updateCardState: UpdateCardState,
    onChoose: (UpdatePending) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && updateCardState is UpdateCardState.UpdateFromFile) {
            onChoose(UpdatePending.URI(uri, context, updateCardState.flipperVersion))
        }
    }
    ComposableUpdateButtonContent(
        buttonModifier = modifier.clickableRipple { launcher.launch("*/*") },
        textId = R.string.updater_card_updater_button_choose_file,
        descriptionId = R.string.updater_card_updater_button_choose_file_desc,
        color = LocalPallet.current.accent
    )
}
