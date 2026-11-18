package dev.namelessnanashi.flipperzero.nfceditor.impl.composable.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.namelessnanashi.flipperzero.nfceditor.impl.R
import dev.namelessnanashi.flipperzero.nfceditor.impl.composable.ComposableNfcCell
import dev.namelessnanashi.flipperzero.nfceditor.impl.composable.FONT_SIZE_SP
import dev.namelessnanashi.flipperzero.nfceditor.impl.composable.PADDING_CELL_DP
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.CardFieldInfo
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.EditorField
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.NfcCellType
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.NfcEditorCardInfo
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.NfcEditorCellLocation

@Composable
fun ComposableAdditionalInfoCard(
    nfcEditorCardInfo: NfcEditorCardInfo,
    currentActiveCell: NfcEditorCellLocation?,
    scaleFactor: Float,
    onClick: (NfcEditorCellLocation) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(12.dp)) {
        ComposableCardFieldInfo(
            nfcEditorCardInfo,
            CardFieldInfo.UID,
            currentActiveCell,
            scaleFactor,
            onClick
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ComposableCardFieldInfo(
                nfcEditorCardInfo,
                CardFieldInfo.ATQA,
                currentActiveCell,
                scaleFactor,
                onClick
            )
            ComposableCardFieldInfo(
                nfcEditorCardInfo,
                CardFieldInfo.SAK,
                currentActiveCell,
                scaleFactor,
                onClick
            )
        }
    }
}

@Composable
private fun ComposableCardFieldInfo(
    cardInfo: NfcEditorCardInfo,
    fieldInfo: CardFieldInfo,
    currentActiveCell: NfcEditorCellLocation?,
    scaleFactor: Float,
    onClick: (NfcEditorCellLocation) -> Unit
) = Row {
    val list = cardInfo.fields[fieldInfo]
    if (list.isEmpty()) {
        return
    }
    val textId = when (fieldInfo) {
        CardFieldInfo.UID -> R.string.nfc_card_uid
        CardFieldInfo.ATQA -> R.string.nfc_card_atqa
        CardFieldInfo.SAK -> R.string.nfc_card_sak
    }
    Text(
        modifier = Modifier.padding(end = (PADDING_CELL_DP * scaleFactor).dp),
        text = stringResource(textId) + ":",
        fontWeight = FontWeight.W700,
        fontSize = (scaleFactor * FONT_SIZE_SP).sp
    )
    list.forEachIndexed { index, item ->
        val cellLocation = remember(index) {
            NfcEditorCellLocation(
                field = EditorField.CARD,
                sectorIndex = 0,
                lineIndex = fieldInfo.index,
                columnIndex = index
            )
        }
        ComposableNfcCell(
            cell = item.copy(cellType = NfcCellType.ON_CARD),
            scaleFactor = scaleFactor,
            isActive = currentActiveCell == cellLocation,
            onClick = { onClick(cellLocation) }
        )
    }
}
