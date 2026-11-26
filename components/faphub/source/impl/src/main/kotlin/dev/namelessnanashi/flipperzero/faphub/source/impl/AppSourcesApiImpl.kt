package dev.namelessnanashi.flipperzero.faphub.source.impl

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.faphub.source.api.AppSourcesApi
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<AppSourcesApi>())
class AppSourcesApiImpl @Inject constructor() : AppSourcesApi {
    @Composable
    override fun ToolsEntry() {
        val context = LocalContext.current
        Card(Modifier.fillMaxWidth().padding(16.dp).clickable { context.startActivity(Intent(context, SourceActivity::class.java)) }) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.source_entry), style = LocalTypography.current.titleB18, color = LocalPallet.current.text100)
                Text(stringResource(R.string.source_entry_description), style = LocalTypography.current.bodyM14, color = LocalPallet.current.text60)
            }
        }
    }
}
