package dev.namelessnanashi.flipperzero.firstpair.impl.composable.tos

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableFlipperButton
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.firstpair.impl.R
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

private const val SUPPORT_COLUMNS = 2

private val SUPPORT_LINKS = listOf(
    "GitHub Sponsors" to "https://github.com/sponsors/NanashiTheNameless",
    "Buy Me a Coffee" to "https://buymeacoffee.com/NamelessNanashi",
    "Ko-fi" to "https://ko-fi.com/NanashiTheNameless",
    "Liberapay" to "https://liberapay.com/NamelessNanashi",
    "Throne" to "https://throne.com/NamelessNanashi"
)

private val NOTICE_POINTS = listOf(
    R.string.firstpair_fork_point_1,
    R.string.firstpair_fork_point_2,
    R.string.firstpair_fork_point_3,
    R.string.firstpair_fork_point_4
)

/**
 * Fork: the first launch screen is a notice that this is an unofficial fork (upstream showed a
 * "turn on Bluetooth" picture with Flipper Devices' terms), with the source and support links.
 * How to connect is on the next screen. "I understand" sits at the end of the scrolling content.
 */
@Composable
fun ComposableTOS(onApplyPress: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(LocalPallet.current.background)
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            NoticeHeader()
            Spacer(Modifier.height(24.dp))
            NoticeCard()
            Spacer(Modifier.height(28.dp))
            SectionLabel(stringResource(R.string.firstpair_fork_source_title))
            val sourceUrl = stringResource(R.string.firstpair_fork_source_url)
            LinkButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.firstpair_fork_source),
                iconId = R.drawable.ic_fork_github,
                url = sourceUrl
            )
            Spacer(Modifier.height(24.dp))
            SectionLabel(stringResource(R.string.firstpair_fork_support))
            SupportGrid()
            Spacer(Modifier.height(32.dp))
            // At the end of the scrolling content: the notice has to be scrolled through first
            ComposableFlipperButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.firstpair_fork_button),
                textStyle = LocalTypography.current.buttonB16,
                onClick = onApplyPress
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NoticeHeader() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(LocalPallet.current.text8),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(40.dp),
            painter = painterResource(DesignSystem.drawable.ic_warning),
            contentDescription = null,
            tint = LocalPallet.current.text100
        )
    }
    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.firstpair_fork_title),
        style = LocalTypography.current.titleB24,
        color = LocalPallet.current.text100,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.firstpair_fork_intro),
        style = LocalTypography.current.bodyR16,
        color = LocalPallet.current.text60,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun NoticeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NOTICE_POINTS.forEach { point -> NoticePoint(stringResource(point)) }
        }
    }
}

@Composable
private fun NoticePoint(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp, end = 12.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(LocalPallet.current.text100)
        )
        Text(
            text = text,
            style = LocalTypography.current.bodyR14,
            color = LocalPallet.current.text80
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 10.dp),
        text = text.uppercase(),
        style = LocalTypography.current.subtitleB12,
        color = LocalPallet.current.text40
    )
}

@Composable
private fun SupportGrid() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SUPPORT_LINKS.chunked(SUPPORT_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (name, url) ->
                    LinkButton(
                        modifier = Modifier.weight(1f),
                        text = name,
                        iconId = R.drawable.ic_fork_heart,
                        url = url
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkButton(
    text: String,
    @DrawableRes iconId: Int,
    url: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .border(width = 1.dp, color = LocalPallet.current.text20, shape = shape)
            .clickableRipple { runCatching { uriHandler.openUri(url) } }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(iconId),
            contentDescription = null,
            tint = LocalPallet.current.text100
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = text,
            style = LocalTypography.current.bodySB14,
            color = LocalPallet.current.text100,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ComposableTOSPreview() {
    FlipperThemeInternal {
        ComposableTOS(onApplyPress = {})
    }
}
