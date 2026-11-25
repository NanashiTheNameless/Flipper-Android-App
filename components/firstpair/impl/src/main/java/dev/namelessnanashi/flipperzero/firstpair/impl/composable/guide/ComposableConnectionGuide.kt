package dev.namelessnanashi.flipperzero.firstpair.impl.composable.guide

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableFlipperButton
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.firstpair.impl.R
import dev.namelessnanashi.flipperzero.firstpair.impl.composable.common.ComposableSearchingAppBar

/**
 * @param onUseUsb null hides the "Use USB" button (the guide opened outside first setup)
 * @param usbNotFound the last "Use USB" tap found no Flipper on the cable
 */
@Composable
internal fun ComposableConnectionGuide(
    onBack: () -> Unit,
    onUseUsb: (() -> Unit)?,
    usbNotFound: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .background(LocalPallet.current.background)
            .navigationBarsPadding()
    ) {
        ComposableSearchingAppBar(stringResource(R.string.firstpair_guide_title), onBack)
        LazyColumn(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            guideSection(bluetoothGuide, topPadding = 24)
            guideSection(usbGuide)
            if (onUseUsb != null) {
                item(key = "use_usb") {
                    UseUsbButton(onUseUsb = onUseUsb, usbNotFound = usbNotFound)
                }
            }
            guideSection(troubleshootingGuide)
            item(key = "bottom") { Column(Modifier.padding(bottom = 16.dp)) {} }
        }
    }
}

private fun LazyListScope.guideSection(section: GuideSection, topPadding: Int = 16) {
    item(key = "header_${section.title}") {
        Column(
            modifier = Modifier.padding(top = topPadding.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(section.title),
                style = LocalTypography.current.titleB20,
                color = LocalPallet.current.text100
            )
            Text(
                text = stringResource(section.intro),
                style = LocalTypography.current.bodyR14,
                color = LocalPallet.current.text60
            )
            Divider(
                modifier = Modifier.padding(top = 6.dp),
                color = LocalPallet.current.divider12
            )
        }
    }
    itemsIndexed(section.steps, key = { index, _ -> "step_${section.title}_$index" }) { index, step ->
        GuideStepItem(index, step)
    }
}

@Composable
private fun GuideStepItem(index: Int, step: GuideStep) {
    Row {
        Text(
            modifier = Modifier.padding(end = 6.dp),
            text = "${index + 1}.",
            style = LocalTypography.current.bodySB14.copy(color = LocalPallet.current.text100)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(step.title),
                style = LocalTypography.current.bodySB14,
                color = LocalPallet.current.text100
            )
            if (step.description != null) {
                Text(
                    text = stringResource(step.description),
                    style = LocalTypography.current.bodyR14,
                    color = LocalPallet.current.text60
                )
            }
            step.action?.let { GuideActionItem(it) }
        }
    }
}

private val MARKDOWN_LINK = Regex("""\[(.+)]\((.+)\)""")

/**
 * Links and settings shortcuts as outlined buttons (markdown links rendered almost invisible
 * on the dark theme)
 */
@Composable
private fun GuideActionItem(action: GuideAction) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    when (action) {
        is GuideAction.Link -> {
            val markdown = stringResource(action.markdown)
            val match = MARKDOWN_LINK.find(markdown)
            val label = match?.groupValues?.get(1) ?: markdown
            val url = match?.groupValues?.get(2)
            GuideButton(text = label) {
                if (url != null) runCatching { uriHandler.openUri(url) }
            }
        }
        is GuideAction.SystemSettings -> GuideButton(text = stringResource(action.label)) {
            try {
                context.startActivity(Intent(action.intentAction))
            } catch (e: ActivityNotFoundException) {
                error(e) { "No activity for ${action.intentAction}" }
            }
        }
    }
}

@Composable
private fun GuideButton(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(30.dp)
    Text(
        modifier = Modifier
            .padding(top = 4.dp)
            .clip(shape)
            .border(width = 1.dp, color = LocalPallet.current.text30, shape = shape)
            .clickableRipple(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = text,
        style = LocalTypography.current.bodySB14,
        color = LocalPallet.current.text100
    )
}

@Composable
private fun UseUsbButton(onUseUsb: () -> Unit, usbNotFound: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ComposableFlipperButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.firstpair_guide_usb_button),
            textStyle = LocalTypography.current.buttonB16,
            onClick = onUseUsb
        )
        if (usbNotFound) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.firstpair_guide_usb_not_found),
                style = LocalTypography.current.bodyR14,
                color = LocalPallet.current.text60,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ComposableConnectionGuidePreview() {
    FlipperThemeInternal {
        ComposableConnectionGuide(onBack = {}, onUseUsb = {}, usbNotFound = true)
    }
}
