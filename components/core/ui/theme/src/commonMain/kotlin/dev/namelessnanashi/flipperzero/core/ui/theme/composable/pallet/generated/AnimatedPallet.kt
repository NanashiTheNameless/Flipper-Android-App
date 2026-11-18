package dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.BrunchCustom
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.BrunchDev
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.BrunchRc
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.BrunchRelease
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwInstall
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwUpdate
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.Neutral.Quaternary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.Neutral.Quinary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.OnColor
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Illustration
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Illustration.Transparent
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.BackgroundMain
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Border.AccentBrand
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Border.Default
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.BottomBar
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.ContentCard
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.ContentCard.TextField
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Dialog
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Dialog.IllustrationField
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Fade
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Menu
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.NavBar
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.PopUp
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.SearchBar
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.SearchBar.SearchField
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Sheet
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Text.Caption
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Text.Label
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Text.Link
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Text.Semantic
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Text.Title
import kotlin.Int
import kotlin.Suppress
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.BlackAndWhite as ActionBlackAndWhite
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.BlackAndWhite.Border as BlackAndWhiteBorder
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.BlackAndWhite.Icon as BlackAndWhiteIcon
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.BlackAndWhite.Text as BlackAndWhiteText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Blue as ActionBlue
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Blue.Background as BlueBackground
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Blue.Background.Primary as BlueBackgroundPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Blue.Background.Secondary as BlueBackgroundSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Blue.Background.Tertiary as BlueBackgroundTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Blue.Border as BlueBorder
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Blue.Border.Primary as BlueBorderPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Blue.Border.Secondary as BlueBorderSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Blue.Border.Tertiary as BlueBorderTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Blue.Icon as BlueIcon
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Blue.Text as BlueText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Brand as ActionBrand
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Brand.Background as BrandBackground
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Brand.Background.Primary as BrandBackgroundPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Brand.Background.Secondary as BrandBackgroundSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Brand.Background.Tertiary as BrandBackgroundTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Brand.Border as BrandBorder
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Brand.Border.Primary as BrandBorderPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Brand.Border.Secondary as BrandBorderSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Brand.Border.Tertiary as BrandBorderTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Brand.Icon as BrandIcon
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Brand.Text as BrandText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.BrunchCustom.Text as BrunchCustomText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.BrunchDev.Text as BrunchDevText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.BrunchRc.Text as BrunchRcText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.BrunchRelease.Text as BrunchReleaseText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Danger as ActionDanger
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Danger.Background as DangerBackground
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Danger.Background.Primary as DangerBackgroundPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Danger.Background.Secondary as DangerBackgroundSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Danger.Background.Tertiary as DangerBackgroundTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Danger.Border as DangerBorder
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Danger.Border.Primary as DangerBorderPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Danger.Border.Secondary as DangerBorderSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Danger.Border.Tertiary as DangerBorderTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Danger.Icon as DangerIcon
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Danger.Text as DangerText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwInstall.Background as FwInstallBackground
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwInstall.Background.Primary as FwInstallBackgroundPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwInstall.Background.Secondary as FwInstallBackgroundSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwInstall.Background.Tertiary as FwInstallBackgroundTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwInstall.Border as FwInstallBorder
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwInstall.Border.Primary as FwInstallBorderPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwInstall.Border.Secondary as FwInstallBorderSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwInstall.Border.Tertiary as FwInstallBorderTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwInstall.Icon as FwInstallIcon
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwInstall.Text as FwInstallText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwUpdate.Background as FwUpdateBackground
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwUpdate.Background.Primary as FwUpdateBackgroundPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwUpdate.Background.Secondary as FwUpdateBackgroundSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwUpdate.Background.Tertiary as FwUpdateBackgroundTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwUpdate.Border as FwUpdateBorder
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwUpdate.Border.Primary as FwUpdateBorderPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwUpdate.Border.Secondary as FwUpdateBorderSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwUpdate.Border.Tertiary as FwUpdateBorderTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwUpdate.Icon as FwUpdateIcon
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.FwUpdate.Text as FwUpdateText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral as ActionNeutral
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Background as NeutralBackground
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Background.Primary as NeutralBackgroundPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Background.Secondary as NeutralBackgroundSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Background.Tertiary as NeutralBackgroundTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Border as NeutralBorder
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Border.Primary as NeutralBorderPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Border.Secondary as NeutralBorderSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Border.Tertiary as NeutralBorderTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Icon as NeutralIcon
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Icon.Primary as NeutralIconPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Icon.Secondary as NeutralIconSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Icon.Tertiary as NeutralIconTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Text as NeutralText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Text.Primary as NeutralTextPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Text.Secondary as NeutralTextSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Neutral.Text.Tertiary as NeutralTextTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Success as ActionSuccess
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Success.Background as SuccessBackground
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Success.Background.Primary as SuccessBackgroundPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Success.Background.Secondary as SuccessBackgroundSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Success.Background.Tertiary as SuccessBackgroundTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Success.Border as SuccessBorder
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Success.Border.Primary as SuccessBorderPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Success.Border.Secondary as SuccessBorderSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Success.Border.Tertiary as SuccessBorderTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Success.Icon as SuccessIcon
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Success.Text as SuccessText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Warning as ActionWarning
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Warning.Background as WarningBackground
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Warning.Background.Primary as WarningBackgroundPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Warning.Background.Secondary as WarningBackgroundSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Warning.Background.Tertiary as WarningBackgroundTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Warning.Border as WarningBorder
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Warning.Border.Primary as WarningBorderPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Warning.Border.Secondary as WarningBorderSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Warning.Border.Tertiary as WarningBorderTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Warning.Icon as WarningIcon
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Action.Warning.Text as WarningText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Background as CategoryBackground
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Background.BadUsb as BackgroundBadUsb
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Background.Bluetooth as BackgroundBluetooth
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Background.Games as BackgroundGames
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Background.Gpio as BackgroundGpio
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Background.Ibutton as BackgroundIbutton
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Background.Infrared as BackgroundInfrared
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Background.Media as BackgroundMedia
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Background.Nfc as BackgroundNfc
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Background.Rfid as BackgroundRfid
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Background.SubGhz as BackgroundSubGhz
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Background.Tools as BackgroundTools
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Icon as CategoryIcon
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Icon.BadUsb as IconBadUsb
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Icon.Bluetooth as IconBluetooth
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Icon.Games as IconGames
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Icon.Gpio as IconGpio
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Icon.Ibutton as IconIbutton
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Icon.Infrared as IconInfrared
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Icon.Media as IconMedia
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Icon.Nfc as IconNfc
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Icon.Rfid as IconRfid
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Icon.SubGhz as IconSubGhz
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Icon.Tools as IconTools
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Text as CategoryText
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Text.BadUsb as TextBadUsb
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Text.Bluetooth as TextBluetooth
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Text.Games as TextGames
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Text.Gpio as TextGpio
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Text.Ibutton as TextIbutton
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Text.Infrared as TextInfrared
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Text.Media as TextMedia
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Text.Nfc as TextNfc
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Text.Rfid as TextRfid
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Text.SubGhz as TextSubGhz
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Category.Text.Tools as TextTools
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon as FlipperPalletV2Icon
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.BlackAndWhite as IconBlackAndWhite
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.Blue as IconBlue
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.Brand as IconBrand
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.Danger as IconDanger
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.Neutral as IconNeutral
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.Neutral.Primary as IconNeutralPrimary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.Neutral.Secondary as IconNeutralSecondary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.Neutral.Tertiary as IconNeutralTertiary
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.OnColor.Black as OnColorBlack
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.OnColor.White as OnColorWhite
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.Success as IconSuccess
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Icon.Warning as IconWarning
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Illustration.BlackAndWhite as IllustrationBlackAndWhite
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Illustration.Blue as IllustrationBlue
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Illustration.Brand as IllustrationBrand
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Illustration.Danger as IllustrationDanger
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Illustration.Neutral as IllustrationNeutral
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Illustration.Success as IllustrationSuccess
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Illustration.Transparent.Black as TransparentBlack
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Illustration.Transparent.White as TransparentWhite
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Illustration.Warning as IllustrationWarning
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Border as SurfaceBorder
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.ContentCard.Body as ContentCardBody
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.ContentCard.Separator as ContentCardSeparator
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Dialog.Body as DialogBody
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Dialog.Separator as DialogSeparator
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Fade.TransparentBlack as FadeTransparentBlack
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Fade.TransparentWhite as FadeTransparentWhite
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Menu.Body as MenuBody
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Menu.Separator as MenuSeparator
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.NavBar.Body as NavBarBody
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.PopUp.Body as PopUpBody
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.SearchBar.Body as SearchBarBody
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Sheet.Body as SheetBody
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Surface.Sheet.Separator as SheetSeparator
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Text as FlipperPalletV2Text
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2.Text.Body as TextBody

private const val ANIMATION_DURATION_MS: Int = 750

private val animationSpec: AnimationSpec<Color> = tween(ANIMATION_DURATION_MS)

@Composable
private fun animatedColor(targetValue: Color): Color = animateColorAsState(
    targetValue =
    targetValue,
    animationSpec = animationSpec
).value

/**
 * Autogenerated code from https://github.com/LionZXY/FlipperPalletGenerator/
 */
@Suppress("LongMethod")
@Composable
internal fun FlipperPalletV2.toAnimatePallet(): FlipperPalletV2 = FlipperPalletV2(
    text = FlipperPalletV2Text(
        title = Title(
            primary = animatedColor(text.title.primary),
            secondary = animatedColor(text.title.secondary),
            tertiary = animatedColor(text.title.tertiary),
            whiteOnColor = animatedColor(text.title.whiteOnColor),
            blackOnColor = animatedColor(text.title.blackOnColor)
        ),
        body = TextBody(
            primary = animatedColor(text.body.primary),
            secondary = animatedColor(text.body.secondary),
            tertiary = animatedColor(text.body.tertiary),
            whiteOnColor = animatedColor(text.body.whiteOnColor),
            blackOnColor = animatedColor(text.body.blackOnColor)
        ),
        label = Label(
            primary = animatedColor(text.label.primary),
            secondary = animatedColor(text.label.secondary),
            tertiary = animatedColor(text.label.tertiary),
            whiteOnColor = animatedColor(text.label.whiteOnColor),
            blackOnColor = animatedColor(text.label.blackOnColor)
        ),
        caption = Caption(
            primary = animatedColor(text.caption.primary),
            secondary = animatedColor(text.caption.secondary),
            tertiary = animatedColor(text.caption.tertiary),
            whiteOnColor = animatedColor(text.caption.whiteOnColor),
            blackOnColor = animatedColor(text.caption.blackOnColor)
        ),
        semantic = Semantic(
            success = animatedColor(text.semantic.success),
            warning = animatedColor(text.semantic.warning),
            danger = animatedColor(text.semantic.danger)
        ),
        link = Link(
            default = animatedColor(text.link.default),
            disabled = animatedColor(text.link.disabled)
        )
    ),
    surface = Surface(
        fade = Fade(
            transparentBlack = FadeTransparentBlack(
                primary = animatedColor(surface.fade.transparentBlack.primary),
                secondary = animatedColor(surface.fade.transparentBlack.secondary),
                tertiary = animatedColor(surface.fade.transparentBlack.tertiary)
            ),
            transparentWhite = FadeTransparentWhite(
                primary = animatedColor(surface.fade.transparentWhite.primary),
                secondary = animatedColor(surface.fade.transparentWhite.secondary),
                tertiary = animatedColor(surface.fade.transparentWhite.tertiary)
            )
        ),
        backgroundMain = BackgroundMain(
            body = animatedColor(surface.backgroundMain.body),
            separator = animatedColor(surface.backgroundMain.separator)
        ),
        navBar = NavBar(
            body = NavBarBody(
                main = animatedColor(surface.navBar.body.main),
                accentBrand = animatedColor(surface.navBar.body.accentBrand)
            )
        ),
        menu = Menu(
            body = MenuBody(
                dufault = animatedColor(surface.menu.body.dufault)
            ),
            separator = MenuSeparator(
                default = animatedColor(surface.menu.separator.default)
            )
        ),
        contentCard = ContentCard(
            body = ContentCardBody(
                default = animatedColor(surface.contentCard.body.default)
            ),
            textField = TextField(
                default = animatedColor(surface.contentCard.textField.default),
                danger = animatedColor(surface.contentCard.textField.danger),
                onColor = animatedColor(surface.contentCard.textField.onColor),
                selected = animatedColor(surface.contentCard.textField.selected)
            ),
            separator = ContentCardSeparator(
                default = animatedColor(surface.contentCard.separator.default)
            )
        ),
        bottomBar = BottomBar(
            body = animatedColor(surface.bottomBar.body),
            tabSelected = animatedColor(surface.bottomBar.tabSelected),
            separator = animatedColor(surface.bottomBar.separator)
        ),
        sheet = Sheet(
            body = SheetBody(
                default = animatedColor(surface.sheet.body.default)
            ),
            separator = SheetSeparator(
                default = animatedColor(surface.sheet.separator.default)
            )
        ),
        dialog = Dialog(
            body = DialogBody(
                default = animatedColor(surface.dialog.body.default)
            ),
            illustrationField = IllustrationField(
                default = animatedColor(surface.dialog.illustrationField.default)
            ),
            separator = DialogSeparator(
                default = animatedColor(surface.dialog.separator.default)
            )
        ),
        popUp = PopUp(
            body = PopUpBody(
                default = animatedColor(surface.popUp.body.default)
            )
        ),
        searchBar = SearchBar(
            body = SearchBarBody(
                default = animatedColor(surface.searchBar.body.default)
            ),
            searchField = SearchField(
                default = animatedColor(surface.searchBar.searchField.default)
            )
        ),
        border = SurfaceBorder(
            default = Default(
                primary = animatedColor(surface.border.default.primary),
                secondary = animatedColor(surface.border.default.secondary),
                tertiary = animatedColor(surface.border.default.tertiary)
            ),
            accentBrand = AccentBrand(
                primary = animatedColor(surface.border.accentBrand.primary),
                secondary = animatedColor(surface.border.accentBrand.secondary),
                tertiary = animatedColor(surface.border.accentBrand.tertiary)
            )
        )
    ),
    action = Action(
        brunchRelease = BrunchRelease(
            text = BrunchReleaseText(
                default = animatedColor(action.brunchRelease.text.default),
                disabled = animatedColor(action.brunchRelease.text.disabled)
            )
        ),
        fwInstall = FwInstall(
            background = FwInstallBackground(
                primary = FwInstallBackgroundPrimary(
                    default = animatedColor(action.fwInstall.background.primary.default),
                    disabled = animatedColor(action.fwInstall.background.primary.disabled)
                ),
                secondary = FwInstallBackgroundSecondary(
                    default = animatedColor(action.fwInstall.background.secondary.default),
                    disabled = animatedColor(action.fwInstall.background.secondary.disabled)
                ),
                tertiary = FwInstallBackgroundTertiary(
                    default = animatedColor(action.fwInstall.background.tertiary.default),
                    disabled = animatedColor(action.fwInstall.background.tertiary.disabled)
                )
            ),
            text = FwInstallText(
                default = animatedColor(action.fwInstall.text.default),
                onColor = animatedColor(action.fwInstall.text.onColor),
                disabled = animatedColor(action.fwInstall.text.disabled)
            ),
            icon = FwInstallIcon(
                default = animatedColor(action.fwInstall.icon.default),
                onColor = animatedColor(action.fwInstall.icon.onColor),
                disabled = animatedColor(action.fwInstall.icon.disabled)
            ),
            border = FwInstallBorder(
                primary = FwInstallBorderPrimary(
                    default = animatedColor(action.fwInstall.border.primary.default),
                    disabled = animatedColor(action.fwInstall.border.primary.disabled)
                ),
                color = animatedColor(action.fwInstall.border.color),
                secondary = FwInstallBorderSecondary(
                    default = animatedColor(action.fwInstall.border.secondary.default),
                    disabled = animatedColor(action.fwInstall.border.secondary.disabled)
                ),
                tertiary = FwInstallBorderTertiary(
                    default = animatedColor(action.fwInstall.border.tertiary.default),
                    disabled = animatedColor(action.fwInstall.border.tertiary.disabled)
                )
            )
        ),
        blue = ActionBlue(
            background = BlueBackground(
                primary = BlueBackgroundPrimary(
                    default = animatedColor(action.blue.background.primary.default),
                    disabled = animatedColor(action.blue.background.primary.disabled)
                ),
                tertiary = BlueBackgroundTertiary(
                    default = animatedColor(action.blue.background.tertiary.default),
                    disabled = animatedColor(action.blue.background.tertiary.disabled)
                ),
                secondary = BlueBackgroundSecondary(
                    default = animatedColor(action.blue.background.secondary.default),
                    disabled = animatedColor(action.blue.background.secondary.disabled)
                )
            ),
            text = BlueText(
                onColor = animatedColor(action.blue.text.onColor),
                default = animatedColor(action.blue.text.default),
                disabled = animatedColor(action.blue.text.disabled)
            ),
            icon = BlueIcon(
                default = animatedColor(action.blue.icon.default),
                onColor = animatedColor(action.blue.icon.onColor),
                disabled = animatedColor(action.blue.icon.disabled)
            ),
            border = BlueBorder(
                tertiary = BlueBorderTertiary(
                    disabled = animatedColor(action.blue.border.tertiary.disabled),
                    default = animatedColor(action.blue.border.tertiary.default)
                ),
                primary = BlueBorderPrimary(
                    default = animatedColor(action.blue.border.primary.default),
                    disabled = animatedColor(action.blue.border.primary.disabled)
                ),
                secondary = BlueBorderSecondary(
                    default = animatedColor(action.blue.border.secondary.default),
                    disabled = animatedColor(action.blue.border.secondary.disabled)
                )
            )
        ),
        neutral = ActionNeutral(
            text = NeutralText(
                primary = NeutralTextPrimary(
                    default = animatedColor(action.neutral.text.primary.default),
                    onColor = animatedColor(action.neutral.text.primary.onColor),
                    disabled = animatedColor(action.neutral.text.primary.disabled)
                ),
                secondary = NeutralTextSecondary(
                    onColor = animatedColor(action.neutral.text.secondary.onColor),
                    disabled = animatedColor(action.neutral.text.secondary.disabled),
                    default = animatedColor(action.neutral.text.secondary.default)
                ),
                tertiary = NeutralTextTertiary(
                    default = animatedColor(action.neutral.text.tertiary.default),
                    disabled = animatedColor(action.neutral.text.tertiary.disabled),
                    onColor = animatedColor(action.neutral.text.tertiary.onColor)
                )
            ),
            icon = NeutralIcon(
                primary = NeutralIconPrimary(
                    default = animatedColor(action.neutral.icon.primary.default),
                    onColor = animatedColor(action.neutral.icon.primary.onColor),
                    disabled = animatedColor(action.neutral.icon.primary.disabled)
                ),
                secondary = NeutralIconSecondary(
                    onColor = animatedColor(action.neutral.icon.secondary.onColor),
                    disabled = animatedColor(action.neutral.icon.secondary.disabled),
                    default = animatedColor(action.neutral.icon.secondary.default)
                ),
                tertiary = NeutralIconTertiary(
                    default = animatedColor(action.neutral.icon.tertiary.default),
                    onColor = animatedColor(action.neutral.icon.tertiary.onColor),
                    disabled = animatedColor(action.neutral.icon.tertiary.disabled)
                )
            ),
            background = NeutralBackground(
                primary = NeutralBackgroundPrimary(
                    default = animatedColor(action.neutral.background.primary.default),
                    disabled = animatedColor(action.neutral.background.primary.disabled)
                ),
                secondary = NeutralBackgroundSecondary(
                    default = animatedColor(action.neutral.background.secondary.default),
                    disabled = animatedColor(action.neutral.background.secondary.disabled)
                ),
                tertiary = NeutralBackgroundTertiary(
                    default = animatedColor(action.neutral.background.tertiary.default),
                    disabled = animatedColor(action.neutral.background.tertiary.disabled)
                )
            ),
            border = NeutralBorder(
                primary = NeutralBorderPrimary(
                    default = animatedColor(action.neutral.border.primary.default),
                    disabled = animatedColor(action.neutral.border.primary.disabled)
                ),
                tertiary = NeutralBorderTertiary(
                    default = animatedColor(action.neutral.border.tertiary.default),
                    tertiary = animatedColor(action.neutral.border.tertiary.tertiary)
                ),
                secondary = NeutralBorderSecondary(
                    default = animatedColor(action.neutral.border.secondary.default),
                    disabled = animatedColor(action.neutral.border.secondary.disabled)
                )
            )
        ),
        brand = ActionBrand(
            background = BrandBackground(
                primary = BrandBackgroundPrimary(
                    default = animatedColor(action.brand.background.primary.default),
                    disabled = animatedColor(action.brand.background.primary.disabled)
                ),
                secondary = BrandBackgroundSecondary(
                    default = animatedColor(action.brand.background.secondary.default),
                    disabled = animatedColor(action.brand.background.secondary.disabled)
                ),
                tertiary = BrandBackgroundTertiary(
                    default = animatedColor(action.brand.background.tertiary.default),
                    disabled = animatedColor(action.brand.background.tertiary.disabled)
                )
            ),
            text = BrandText(
                default = animatedColor(action.brand.text.default),
                onColor = animatedColor(action.brand.text.onColor),
                disabled = animatedColor(action.brand.text.disabled)
            ),
            border = BrandBorder(
                primary = BrandBorderPrimary(
                    default = animatedColor(action.brand.border.primary.default),
                    disabled = animatedColor(action.brand.border.primary.disabled)
                ),
                secondary = BrandBorderSecondary(
                    default = animatedColor(action.brand.border.secondary.default),
                    disabled = animatedColor(action.brand.border.secondary.disabled)
                ),
                tertiary = BrandBorderTertiary(
                    default = animatedColor(action.brand.border.tertiary.default),
                    tertiary = animatedColor(action.brand.border.tertiary.tertiary)
                )
            ),
            icon = BrandIcon(
                default = animatedColor(action.brand.icon.default),
                onColor = animatedColor(action.brand.icon.onColor),
                disabled = animatedColor(action.brand.icon.disabled)
            )
        ),
        fwUpdate = FwUpdate(
            background = FwUpdateBackground(
                primary = FwUpdateBackgroundPrimary(
                    default = animatedColor(action.fwUpdate.background.primary.default),
                    disabled = animatedColor(action.fwUpdate.background.primary.disabled)
                ),
                secondary = FwUpdateBackgroundSecondary(
                    default = animatedColor(action.fwUpdate.background.secondary.default),
                    disabled = animatedColor(action.fwUpdate.background.secondary.disabled)
                ),
                tertiary = FwUpdateBackgroundTertiary(
                    default = animatedColor(action.fwUpdate.background.tertiary.default),
                    disabled = animatedColor(action.fwUpdate.background.tertiary.disabled)
                ),
                onColor = animatedColor(action.fwUpdate.background.onColor)
            ),
            text = FwUpdateText(
                default = animatedColor(action.fwUpdate.text.default),
                onColor = animatedColor(action.fwUpdate.text.onColor),
                disabled = animatedColor(action.fwUpdate.text.disabled)
            ),
            icon = FwUpdateIcon(
                default = animatedColor(action.fwUpdate.icon.default),
                onColor = animatedColor(action.fwUpdate.icon.onColor),
                disabled = animatedColor(action.fwUpdate.icon.disabled)
            ),
            border = FwUpdateBorder(
                primary = FwUpdateBorderPrimary(
                    default = animatedColor(action.fwUpdate.border.primary.default),
                    disabled = animatedColor(action.fwUpdate.border.primary.disabled)
                ),
                secondary = FwUpdateBorderSecondary(
                    default = animatedColor(action.fwUpdate.border.secondary.default),
                    disabled = animatedColor(action.fwUpdate.border.secondary.disabled)
                ),
                tertiary = FwUpdateBorderTertiary(
                    default = animatedColor(action.fwUpdate.border.tertiary.default),
                    disabled = animatedColor(action.fwUpdate.border.tertiary.disabled)
                )
            )
        ),
        brunchRc = BrunchRc(
            text = BrunchRcText(
                default = animatedColor(action.brunchRc.text.default),
                disabled = animatedColor(action.brunchRc.text.disabled)
            )
        ),
        brunchDev = BrunchDev(
            text = BrunchDevText(
                default = animatedColor(action.brunchDev.text.default),
                disabled = animatedColor(action.brunchDev.text.disabled)
            )
        ),
        brunchCustom = BrunchCustom(
            text = BrunchCustomText(
                default = animatedColor(action.brunchCustom.text.default),
                disabled = animatedColor(action.brunchCustom.text.disabled)
            )
        ),
        danger = ActionDanger(
            text = DangerText(
                default = animatedColor(action.danger.text.default),
                onColor = animatedColor(action.danger.text.onColor),
                disabled = animatedColor(action.danger.text.disabled)
            ),
            icon = DangerIcon(
                default = animatedColor(action.danger.icon.default),
                onColor = animatedColor(action.danger.icon.onColor),
                disabled = animatedColor(action.danger.icon.disabled)
            ),
            border = DangerBorder(
                primary = DangerBorderPrimary(
                    default = animatedColor(action.danger.border.primary.default),
                    disabled = animatedColor(action.danger.border.primary.disabled)
                ),
                secondary = DangerBorderSecondary(
                    default = animatedColor(action.danger.border.secondary.default),
                    disabled = animatedColor(action.danger.border.secondary.disabled)
                ),
                tertiary = DangerBorderTertiary(
                    default = animatedColor(action.danger.border.tertiary.default),
                    disabled = animatedColor(action.danger.border.tertiary.disabled)
                )
            ),
            background = DangerBackground(
                tertiary = DangerBackgroundTertiary(
                    default = animatedColor(action.danger.background.tertiary.default),
                    disabled = animatedColor(action.danger.background.tertiary.disabled)
                ),
                primary = DangerBackgroundPrimary(
                    default = animatedColor(action.danger.background.primary.default),
                    disabled = animatedColor(action.danger.background.primary.disabled)
                ),
                secondary = DangerBackgroundSecondary(
                    default = animatedColor(action.danger.background.secondary.default),
                    disabled = animatedColor(action.danger.background.secondary.disabled)
                )
            )
        ),
        success = ActionSuccess(
            background = SuccessBackground(
                primary = SuccessBackgroundPrimary(
                    default = animatedColor(action.success.background.primary.default),
                    disabled = animatedColor(action.success.background.primary.disabled)
                ),
                secondary = SuccessBackgroundSecondary(
                    default = animatedColor(action.success.background.secondary.default),
                    disabled = animatedColor(action.success.background.secondary.disabled)
                ),
                tertiary = SuccessBackgroundTertiary(
                    default = animatedColor(action.success.background.tertiary.default),
                    disabled = animatedColor(action.success.background.tertiary.disabled)
                )
            ),
            text = SuccessText(
                default = animatedColor(action.success.text.default),
                onColor = animatedColor(action.success.text.onColor),
                disabled = animatedColor(action.success.text.disabled)
            ),
            icon = SuccessIcon(
                default = animatedColor(action.success.icon.default),
                onColor = animatedColor(action.success.icon.onColor),
                disabled = animatedColor(action.success.icon.disabled)
            ),
            border = SuccessBorder(
                primary = SuccessBorderPrimary(
                    default = animatedColor(action.success.border.primary.default),
                    disabled = animatedColor(action.success.border.primary.disabled)
                ),
                secondary = SuccessBorderSecondary(
                    default = animatedColor(action.success.border.secondary.default),
                    disabled = animatedColor(action.success.border.secondary.disabled)
                ),
                tertiary = SuccessBorderTertiary(
                    default = animatedColor(action.success.border.tertiary.default),
                    disabled = animatedColor(action.success.border.tertiary.disabled)
                )
            )
        ),
        warning = ActionWarning(
            background = WarningBackground(
                primary = WarningBackgroundPrimary(
                    default = animatedColor(action.warning.background.primary.default),
                    disabled = animatedColor(action.warning.background.primary.disabled)
                ),
                secondary = WarningBackgroundSecondary(
                    default = animatedColor(action.warning.background.secondary.default),
                    disabled = animatedColor(action.warning.background.secondary.disabled)
                ),
                tertiary = WarningBackgroundTertiary(
                    default = animatedColor(action.warning.background.tertiary.default),
                    disabled = animatedColor(action.warning.background.tertiary.disabled)
                )
            ),
            text = WarningText(
                default = animatedColor(action.warning.text.default),
                onColor = animatedColor(action.warning.text.onColor),
                disabled = animatedColor(action.warning.text.disabled)
            ),
            icon = WarningIcon(
                default = animatedColor(action.warning.icon.default),
                onColor = animatedColor(action.warning.icon.onColor),
                disabled = animatedColor(action.warning.icon.disabled)
            ),
            border = WarningBorder(
                primary = WarningBorderPrimary(
                    default = animatedColor(action.warning.border.primary.default),
                    disabled = animatedColor(action.warning.border.primary.disabled)
                ),
                secondary = WarningBorderSecondary(
                    default = animatedColor(action.warning.border.secondary.default),
                    disabled = animatedColor(action.warning.border.secondary.disabled)
                ),
                tertiary = WarningBorderTertiary(
                    default = animatedColor(action.warning.border.tertiary.default),
                    disabled = animatedColor(action.warning.border.tertiary.disabled)
                )
            )
        ),
        blackAndWhite = ActionBlackAndWhite(
            text = BlackAndWhiteText(
                default = animatedColor(action.blackAndWhite.text.default),
                disabled = animatedColor(action.blackAndWhite.text.disabled)
            ),
            icon = BlackAndWhiteIcon(
                default = animatedColor(action.blackAndWhite.icon.default),
                disabled = animatedColor(action.blackAndWhite.icon.disabled)
            ),
            border = BlackAndWhiteBorder(
                default = animatedColor(action.blackAndWhite.border.default),
                disabled = animatedColor(action.blackAndWhite.border.disabled),
                blackOnColor = animatedColor(action.blackAndWhite.border.blackOnColor),
                whiteOnColor = animatedColor(action.blackAndWhite.border.whiteOnColor)
            )
        )
    ),
    illustration = Illustration(
        danger = IllustrationDanger(
            primary = animatedColor(illustration.danger.primary),
            secondary = animatedColor(illustration.danger.secondary),
            tertiary = animatedColor(illustration.danger.tertiary)
        ),
        success = IllustrationSuccess(
            primary = animatedColor(illustration.success.primary),
            secondary = animatedColor(illustration.success.secondary),
            tertiary = animatedColor(illustration.success.tertiary)
        ),
        warning = IllustrationWarning(
            primary = animatedColor(illustration.warning.primary),
            secondary = animatedColor(illustration.warning.secondary),
            tertiary = animatedColor(illustration.warning.tertiary)
        ),
        neutral = IllustrationNeutral(
            primary = animatedColor(illustration.neutral.primary),
            quaternary = animatedColor(illustration.neutral.quaternary),
            quinary = animatedColor(illustration.neutral.quinary),
            tertiary = animatedColor(illustration.neutral.tertiary),
            secondary = animatedColor(illustration.neutral.secondary)
        ),
        brand = IllustrationBrand(
            primary = animatedColor(illustration.brand.primary),
            secondary = animatedColor(illustration.brand.secondary),
            tertiary = animatedColor(illustration.brand.tertiary)
        ),
        blue = IllustrationBlue(
            primary = animatedColor(illustration.blue.primary),
            secondary = animatedColor(illustration.blue.secondary),
            tertiary = animatedColor(illustration.blue.tertiary)
        ),
        blackAndWhite = IllustrationBlackAndWhite(
            white = animatedColor(illustration.blackAndWhite.white),
            black = animatedColor(illustration.blackAndWhite.black),
            whiteOnColor = animatedColor(illustration.blackAndWhite.whiteOnColor),
            blackOnColor = animatedColor(illustration.blackAndWhite.blackOnColor)
        ),
        transparent = Transparent(
            black = TransparentBlack(
                primary = animatedColor(illustration.transparent.black.primary),
                secondary = animatedColor(illustration.transparent.black.secondary),
                tertiary = animatedColor(illustration.transparent.black.tertiary)
            ),
            white = TransparentWhite(
                primary = animatedColor(illustration.transparent.white.primary),
                secondary = animatedColor(illustration.transparent.white.secondary),
                tertiary = animatedColor(illustration.transparent.white.tertiary)
            )
        )
    ),
    category = Category(
        background = CategoryBackground(
            subGhz = BackgroundSubGhz(
                default = animatedColor(category.background.subGhz.default),
                disabled = animatedColor(category.background.subGhz.disabled)
            ),
            rfid = BackgroundRfid(
                default = animatedColor(category.background.rfid.default),
                disabled = animatedColor(category.background.rfid.disabled)
            ),
            nfc = BackgroundNfc(
                default = animatedColor(category.background.nfc.default),
                disabled = animatedColor(category.background.nfc.disabled)
            ),
            infrared = BackgroundInfrared(
                default = animatedColor(category.background.infrared.default),
                disabled = animatedColor(category.background.infrared.disabled)
            ),
            ibutton = BackgroundIbutton(
                default = animatedColor(category.background.ibutton.default),
                disabled = animatedColor(category.background.ibutton.disabled)
            ),
            badUsb = BackgroundBadUsb(
                default = animatedColor(category.background.badUsb.default),
                disabled = animatedColor(category.background.badUsb.disabled)
            ),
            gpio = BackgroundGpio(
                default = animatedColor(category.background.gpio.default),
                disabled = animatedColor(category.background.gpio.disabled)
            ),
            games = BackgroundGames(
                default = animatedColor(category.background.games.default),
                disabled = animatedColor(category.background.games.disabled)
            ),
            media = BackgroundMedia(
                default = animatedColor(category.background.media.default),
                disabled = animatedColor(category.background.media.disabled)
            ),
            tools = BackgroundTools(
                default = animatedColor(category.background.tools.default),
                disabled = animatedColor(category.background.tools.disabled)
            ),
            bluetooth = BackgroundBluetooth(
                default = animatedColor(category.background.bluetooth.default),
                disabled = animatedColor(category.background.bluetooth.disabled)
            )
        ),
        icon = CategoryIcon(
            subGhz = IconSubGhz(
                default = animatedColor(category.icon.subGhz.default)
            ),
            rfid = IconRfid(
                default = animatedColor(category.icon.rfid.default)
            ),
            nfc = IconNfc(
                default = animatedColor(category.icon.nfc.default)
            ),
            infrared = IconInfrared(
                default = animatedColor(category.icon.infrared.default)
            ),
            ibutton = IconIbutton(
                default = animatedColor(category.icon.ibutton.default)
            ),
            badUsb = IconBadUsb(
                default = animatedColor(category.icon.badUsb.default)
            ),
            gpio = IconGpio(
                default = animatedColor(category.icon.gpio.default)
            ),
            games = IconGames(
                default = animatedColor(category.icon.games.default)
            ),
            media = IconMedia(
                default = animatedColor(category.icon.media.default)
            ),
            tools = IconTools(
                default = animatedColor(category.icon.tools.default)
            ),
            bluetooth = IconBluetooth(
                default = animatedColor(category.icon.bluetooth.default)
            )
        ),
        text = CategoryText(
            subGhz = TextSubGhz(
                default = animatedColor(category.text.subGhz.default)
            ),
            rfid = TextRfid(
                default = animatedColor(category.text.rfid.default)
            ),
            nfc = TextNfc(
                default = animatedColor(category.text.nfc.default)
            ),
            infrared = TextInfrared(
                default = animatedColor(category.text.infrared.default)
            ),
            ibutton = TextIbutton(
                default = animatedColor(category.text.ibutton.default)
            ),
            badUsb = TextBadUsb(
                default = animatedColor(category.text.badUsb.default)
            ),
            gpio = TextGpio(
                default = animatedColor(category.text.gpio.default)
            ),
            games = TextGames(
                default = animatedColor(category.text.games.default)
            ),
            media = TextMedia(
                default = animatedColor(category.text.media.default)
            ),
            tools = TextTools(
                default = animatedColor(category.text.tools.default)
            ),
            bluetooth = TextBluetooth(
                default = animatedColor(category.text.bluetooth.default)
            )
        )
    ),
    icon = FlipperPalletV2Icon(
        neutral = IconNeutral(
            primary = IconNeutralPrimary(
                default = animatedColor(icon.neutral.primary.default),
                transparent = animatedColor(icon.neutral.primary.transparent)
            ),
            secondary = IconNeutralSecondary(
                default = animatedColor(icon.neutral.secondary.default),
                transparent = animatedColor(icon.neutral.secondary.transparent)
            ),
            tertiary = IconNeutralTertiary(
                default = animatedColor(icon.neutral.tertiary.default),
                transparent = animatedColor(icon.neutral.tertiary.transparent)
            ),
            quaternary = Quaternary(
                default = animatedColor(icon.neutral.quaternary.default)
            ),
            quinary = Quinary(
                default = animatedColor(icon.neutral.quinary.default)
            )
        ),
        onColor = OnColor(
            white = OnColorWhite(
                default = animatedColor(icon.onColor.white.default),
                transparent = animatedColor(icon.onColor.white.transparent)
            ),
            black = OnColorBlack(
                default = animatedColor(icon.onColor.black.default),
                transparent = animatedColor(icon.onColor.black.transparent)
            )
        ),
        success = IconSuccess(
            primary = animatedColor(icon.success.primary),
            secondary = animatedColor(icon.success.secondary),
            tertiary = animatedColor(icon.success.tertiary)
        ),
        warning = IconWarning(
            primary = animatedColor(icon.warning.primary),
            secondary = animatedColor(icon.warning.secondary),
            tertiary = animatedColor(icon.warning.tertiary)
        ),
        danger = IconDanger(
            primary = animatedColor(icon.danger.primary),
            secondary = animatedColor(icon.danger.secondary),
            tertiary = animatedColor(icon.danger.tertiary)
        ),
        blackAndWhite = IconBlackAndWhite(
            default = animatedColor(icon.blackAndWhite.default),
            transparent = animatedColor(icon.blackAndWhite.transparent),
            blackOnColor = animatedColor(icon.blackAndWhite.blackOnColor),
            whiteOnColor = animatedColor(icon.blackAndWhite.whiteOnColor)
        ),
        brand = IconBrand(
            primary = animatedColor(icon.brand.primary),
            secondary = animatedColor(icon.brand.secondary),
            tertiary = animatedColor(icon.brand.tertiary)
        ),
        blue = IconBlue(
            primary = animatedColor(icon.blue.primary),
            secondary = animatedColor(icon.blue.secondary),
            tertiary = animatedColor(icon.blue.tertiary)
        )
    )
)
