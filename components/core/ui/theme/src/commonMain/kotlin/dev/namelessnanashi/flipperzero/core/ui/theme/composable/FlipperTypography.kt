package dev.namelessnanashi.flipperzero.core.ui.theme.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import flipperapp.components.core.ui.theme.generated.resources.Res
import flipperapp.components.core.ui.theme.generated.resources.oxproto_bold
import flipperapp.components.core.ui.theme.generated.resources.oxproto_italic
import flipperapp.components.core.ui.theme.generated.resources.oxproto_regular
import org.jetbrains.compose.resources.Font

@Stable
data class FlipperTypography(
    val titleB24: TextStyle,
    val titleB22: TextStyle,
    val titleB20: TextStyle,
    val titleSB18: TextStyle,
    val titleSB16: TextStyle,
    val titleEB20: TextStyle,
    val titleEB18: TextStyle,
    val titleB18: TextStyle,
    val titleR18: TextStyle,
    val titleM18: TextStyle,
    val subtitleB12: TextStyle,
    val subtitleEB12: TextStyle,
    val subtitleM12: TextStyle,
    val subtitleR12: TextStyle,
    val subtitleR10: TextStyle,
    val subtitleB10: TextStyle,
    val subtitleM10: TextStyle,
    val bodySB14: TextStyle,
    val bodySSB14: TextStyle,
    val bodyR14: TextStyle,
    val bodyM14: TextStyle,
    val bodyR16: TextStyle,
    val buttonB16: TextStyle,
    val buttonM16: TextStyle,
    val buttonB14: TextStyle,
    val updateButton40: TextStyle,
    val updateText40: TextStyle,
    val flipperAction: TextStyle,
    val monoSpaceM14: TextStyle,
    val monoSpaceM10: TextStyle,
    val monoSpaceM12: TextStyle,
    val monoSpaceM16: TextStyle,
    val monoSpaceR12: TextStyle,
    val notificationB8: TextStyle,
    val fapHubButtonText: TextStyle,
    val fapHubButtonProgressText: TextStyle,
    val infraredEditButton: TextStyle,
)

@Composable
@Suppress("LongMethod")
fun getTypography(): FlipperTypography {
    val robotoFamily = getRobotoFamily()
    return FlipperTypography(
        titleB24 = TextStyle(
            fontSize = 24.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W700
        ),
        titleB22 = TextStyle(
            fontSize = 22.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W700
        ),
        titleB20 = TextStyle(
            fontSize = 20.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W700
        ),
        titleSB18 = TextStyle(
            fontSize = 18.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W600
        ),
        titleSB16 = TextStyle(
            fontSize = 18.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W600
        ),
        titleEB20 = TextStyle(
            fontSize = 20.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W800
        ),
        titleEB18 = TextStyle(
            fontSize = 18.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W800
        ),
        titleB18 = TextStyle(
            fontSize = 18.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W700
        ),
        titleR18 = TextStyle(
            fontSize = 18.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W400
        ),
        titleM18 = TextStyle(
            fontSize = 18.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        subtitleB12 = TextStyle(
            fontSize = 12.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W700
        ),
        subtitleEB12 = TextStyle(
            fontSize = 12.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W800
        ),
        subtitleM12 = TextStyle(
            fontSize = 12.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        subtitleR12 = TextStyle(
            fontSize = 12.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W400
        ),
        subtitleR10 = TextStyle(
            fontSize = 10.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W400
        ),
        subtitleB10 = TextStyle(
            fontSize = 10.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W700
        ),
        subtitleM10 = TextStyle(
            fontSize = 10.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        bodySB14 = TextStyle(
            fontSize = 14.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W600
        ),
        bodySSB14 = TextStyle(
            fontSize = 14.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W700
        ),
        bodyR14 = TextStyle(
            fontSize = 14.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W400
        ),
        bodyM14 = TextStyle(
            fontSize = 14.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        bodyR16 = TextStyle(
            fontSize = 16.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W400
        ),
        buttonB16 = TextStyle(
            fontSize = 16.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W700
        ),
        buttonM16 = TextStyle(
            fontSize = 16.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        buttonB14 = TextStyle(
            fontSize = 14.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W700
        ),
        updateButton40 = TextStyle(
            fontSize = 40.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        updateText40 = TextStyle(
            fontSize = 40.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W400
        ),
        flipperAction = TextStyle(
            fontSize = 24.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        monoSpaceM14 = TextStyle(
            fontSize = 14.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        monoSpaceM10 = TextStyle(
            fontSize = 10.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        monoSpaceM12 = TextStyle(
            fontSize = 12.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        monoSpaceM16 = TextStyle(
            fontSize = 16.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        monoSpaceR12 = TextStyle(
            fontSize = 12.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W400
        ),
        notificationB8 = TextStyle(
            fontSize = 8.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W700
        ),
        fapHubButtonText = TextStyle(
            fontSize = 18.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        fapHubButtonProgressText = TextStyle(
            fontSize = 18.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W500
        ),
        infraredEditButton = TextStyle(
            fontSize = 32.sp,
            fontFamily = robotoFamily,
            fontWeight = FontWeight.W400
        ),
    )
}

/**
 * Fork: every text style uses 0xProto (https://github.com/0xType/0xProto, SIL OFL 1.1, see
 * docs/fork/licenses/0xProto-OFL.txt), including the former pixel fonts and Roboto Mono.
 * It ships Regular, Bold and Italic; lighter and medium weights fall back to Regular.
 */
@Composable
fun getOxProtoFamily() = FontFamily(
    Font(Res.font.oxproto_regular, FontWeight.W400),
    Font(Res.font.oxproto_italic, FontWeight.W400, FontStyle.Italic),
    Font(Res.font.oxproto_bold, FontWeight.W700)
)

@Composable
private fun getRobotoFamily() = getOxProtoFamily()
