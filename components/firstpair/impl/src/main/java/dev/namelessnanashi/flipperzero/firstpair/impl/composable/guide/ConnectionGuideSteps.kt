package dev.namelessnanashi.flipperzero.firstpair.impl.composable.guide

import android.provider.Settings
import androidx.annotation.StringRes
import dev.namelessnanashi.flipperzero.firstpair.impl.R

internal sealed interface GuideAction {
    /**
     * Markdown link, `[text](url)`
     */
    data class Link(@StringRes val markdown: Int) : GuideAction

    /**
     * Opens a system settings screen
     */
    data class SystemSettings(@StringRes val label: Int, val intentAction: String) : GuideAction
}

internal data class GuideStep(
    @StringRes val title: Int,
    @StringRes val description: Int? = null,
    val action: GuideAction? = null
)

internal data class GuideSection(
    @StringRes val title: Int,
    @StringRes val intro: Int,
    val steps: List<GuideStep>
)

internal val bluetoothGuide = GuideSection(
    title = R.string.firstpair_guide_ble_title,
    intro = R.string.firstpair_guide_ble_intro,
    steps = listOf(
        GuideStep(
            R.string.firstpair_guide_ble_1_title,
            R.string.firstpair_guide_ble_1_desc,
            GuideAction.Link(R.string.firstpair_guide_ble_1_link)
        ),
        GuideStep(
            R.string.firstpair_guide_ble_2_title,
            R.string.firstpair_guide_ble_2_desc,
            GuideAction.SystemSettings(
                R.string.firstpair_guide_ble_2_action,
                Settings.ACTION_BLUETOOTH_SETTINGS
            )
        ),
        GuideStep(
            R.string.firstpair_guide_ble_3_title,
            R.string.firstpair_guide_ble_3_desc,
            GuideAction.Link(R.string.firstpair_guide_ble_3_link)
        ),
        GuideStep(R.string.firstpair_guide_ble_4_title, R.string.firstpair_guide_ble_4_desc),
        GuideStep(
            R.string.firstpair_guide_ble_5_title,
            R.string.firstpair_guide_ble_5_desc,
            GuideAction.SystemSettings(
                R.string.firstpair_guide_ble_2_action,
                Settings.ACTION_BLUETOOTH_SETTINGS
            )
        ),
        GuideStep(R.string.firstpair_guide_ble_6_title, R.string.firstpair_guide_ble_6_desc)
    )
)

internal val usbGuide = GuideSection(
    title = R.string.firstpair_guide_usb_title,
    intro = R.string.firstpair_guide_usb_intro,
    steps = listOf(
        GuideStep(R.string.firstpair_guide_usb_1_title, R.string.firstpair_guide_usb_1_desc),
        GuideStep(R.string.firstpair_guide_usb_2_title, R.string.firstpair_guide_usb_2_desc),
        GuideStep(R.string.firstpair_guide_usb_3_title, R.string.firstpair_guide_usb_3_desc),
        GuideStep(R.string.firstpair_guide_usb_4_title, R.string.firstpair_guide_usb_4_desc),
        GuideStep(R.string.firstpair_guide_usb_5_title, R.string.firstpair_guide_usb_5_desc)
    )
)

internal val troubleshootingGuide = GuideSection(
    title = R.string.firstpair_guide_more_title,
    intro = R.string.firstpair_guide_more_intro,
    steps = listOf(
        GuideStep(R.string.firstpair_guide_more_1_title, R.string.firstpair_guide_more_1_desc),
        GuideStep(
            R.string.firstpair_guide_more_2_title,
            R.string.firstpair_guide_more_2_desc,
            GuideAction.Link(R.string.firstpair_guide_more_2_link)
        ),
        GuideStep(R.string.firstpair_guide_more_3_title, R.string.firstpair_guide_more_3_desc),
        GuideStep(
            R.string.firstpair_guide_more_4_title,
            R.string.firstpair_guide_more_4_desc,
            GuideAction.Link(R.string.firstpair_guide_more_4_link)
        )
    )
)
