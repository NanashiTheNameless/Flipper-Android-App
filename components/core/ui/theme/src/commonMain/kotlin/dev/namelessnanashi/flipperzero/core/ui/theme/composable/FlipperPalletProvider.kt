package dev.namelessnanashi.flipperzero.core.ui.theme.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getThemedFlipperPallet(): FlipperPallet = darkPallet.toAnimatePallet()

// Fork: true black (OLED); cards #111111, dialogs and sheets #1A1A1A
@Suppress("MagicNumber")
private val darkPallet = FlipperPallet(
    background = Color(0xFF000000),
    appBar = Color(0xFF000000),
    onAppBar = Color(0xFFFFFFFF),
    accent = Color(0xFFE6E6E6),
    accentSecond = Color(0xFFBDBDBD),
    updateProgressBackgroundBlue = Color(0xFF4D4D4D),
    onFlipperButton = Color(0xFF000000),
    actionOnFlipperEnable = Color(0xFFBDBDBD),
    actionOnFlipperProgress = Color(0xFF707070),
    actionOnFlipperSubGhzEnable = Color(0xFFE6E6E6),
    actionOnFlipperSubGhzProgress = Color(0xFF8C8C8C),
    actionOnFlipperInfraredEnable = Color(0xFFE6E6E6),
    actionOnFlipperInfraredProgress = Color(0xFF8C8C8C),
    shareSheetBackgroundAction = Color(0xFFE6E6E6),
    fapHubOpenAppEnable = Color(0xFF707070),
    fapHubOpenAppProgress = Color(0xFFBDBDBD),
    onFapHubInstallButton = Color(0xFF000000),
    tabSwitchActiveColor = Color(0xFF000000),
    error = Color(0xFF000000),
    content = Color(0xFF111111),
    onContent = Color(0xFFFFFFFF),

    text100 = Color(0xFFFFFFFF),
    text88 = Color(0XFFE9E9E9),
    text80 = Color(0XFFDFDFDF),
    text60 = Color(0xFFD6D6D6),
    text40 = Color(0xFFCCCCCC),
    text30 = Color(0xFFC1C1C1),
    text20 = Color(0xFFAAAAAA),
    text16 = Color(0xFF919191),
    text12 = Color(0xFF616161),
    text8 = Color(0xFF616161),
    text4 = Color(0xFF1C1C1C),

    iconTint100 = Color(0xFFFFFFFF),
    iconTint80 = Color(0XFFDFDFDF),
    iconTint30 = Color(0xFFC1C1C1),
    iconTint16 = Color(0xFF919191),

    divider12 = Color(0xFF616161),
    channelFirmwareReleaseCandidate = Color(0xFFAA69FA),
    bottomBarSelected = Color(0xFFE5E5E5),
    bottomBarBackground = Color(0xFF000000),
    disableSwitch = Color(0xFF616161),
    backgroundDialog = Color(0xFF1A1A1A),
    notificationCard = Color(0xFF616161),

    substrateActiveCellNfcEditor = Color(0xFF616161),
    hexKeyboardBackground = Color(0xFF000000),

    shareSheetBackground = Color(0xFF1A1A1A),
    shareSheetScrimColor = Color(0xFFFFFFFF).copy(alpha = 0.15f),
    fapHubSwitchBackground = Color(0xFF101010),
    fapHubSelectedBackgroundColor = Color(0xFF111111),
    fapHubDividerColor = Color(0xFF303030),
    fapHubSortedColor = Color(0xFFC1C1C1),
    fapHubCategoryText = Color(0xFFCCCCCC),
    fapHubActiveColor = Color(0xFF000000),
    fapHubInactiveColor = Color(0xFF919191),
    fapHubDeleteDialogBackground = Color(0xFF1A1A1A),

    fapHubBuildStatusReadyBackground = Color(0xFF16251A),
    fapHubBuildStatusReadyText = Color(0xFF197B34),
    fapHubBuildStatusRebuildingBackground = Color(0xFF242000),
    fapHubBuildStatusRebuildingText = Color(0xFFB69903),
    fapHubBuildStatusFailedBackground = Color(0xFF340006),
    fapHubBuildStatusFailedText = Color(0xFFC82419),

    flipperDisableButton = Color(0xFFAAAAAA),
    reportBorder = Color(0xFF616161),

    screenStreamingBorderColor = Color(0x7FBDBDBD),
    screenStreamingNotConnectedColor = Color(0xFF303030),

    shareSheetStatusBarActiveColor = Color(0xFF000000),
    shareSheetStatusBarDefaultColor = Color(0xFF000000),
    shareSheetNavigationBarActiveColor = Color(0xFF1A1A1A),
    shareSheetNavigationBarDefaultColor = Color(0xFF000000),

    keyScreenDisabled = Color(0xFF616161),

    placeholder = Color(0xFF616161)
)
