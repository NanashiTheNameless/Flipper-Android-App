package dev.namelessnanashi.flipperzero.info.impl.compose.screens.fullinfo

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.RadioStackInfo
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.RadioStackType
import dev.namelessnanashi.flipperzero.core.ktx.jre.isNotNull
import dev.namelessnanashi.flipperzero.info.impl.R
import dev.namelessnanashi.flipperzero.info.shared.ComposableDeviceInfoRowWithText

@Composable
fun ComposableRadioStackInfo(info: RadioStackInfo, inProgress: Boolean) {
    val text = if (isNotNull(info.type, info.radioFirmware)) {
        "${info.radioFirmware} (${getNameRadioStackType(info.type)})"
    } else {
        null
    }
    ComposableDeviceInfoRowWithText(
        titleId = R.string.full_info_radio_stack,
        inProgress = inProgress,
        value = text
    )
}

@Composable
private fun getNameRadioStackType(radioType: RadioStackType?): String {
    val textId = when (radioType) {
        RadioStackType.Full -> R.string.radio_stack_type_full
        RadioStackType.Light -> R.string.radio_stack_type_light
        RadioStackType.Beacon -> R.string.radio_stack_type_beacon
        RadioStackType.Basic -> R.string.radio_stack_type_basic
        RadioStackType.FullExtAdv -> R.string.radio_stack_type_full_ext_adv
        RadioStackType.HCIExtAdv -> R.string.radio_stack_type_hci_ext_adv
        RadioStackType.Unkwown, null -> R.string.radio_stack_type_unknown
    }
    return stringResource(id = textId)
}
