package dev.namelessnanashi.flipperzero.faphub.uninstallbutton.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.faphub.appcard.composable.ComposableAppDialogBox
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItem
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.FapInstallationQueueApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.uninstallbutton.api.FapUninstallApi
import dev.namelessnanashi.flipperzero.faphub.uninstallbutton.impl.composable.ComposableFapUninstall
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<FapUninstallApi>())
class FapUninstallImpl @Inject constructor(
    private val queueApiProvider: Provider<FapInstallationQueueApi>
) : FapUninstallApi {
    @Composable
    override fun ComposableFapUninstallButton(
        modifier: Modifier,
        applicationUid: String,
        dialogAppBox: @Composable (Modifier) -> Unit
    ) {
        ComposableFapUninstall(
            modifier = modifier,
            dialogAppBox = dialogAppBox,
            onDelete = {
                queueApiProvider.invoke().enqueue(FapActionRequest.Delete(applicationUid))
            }
        )
    }

    @Composable
    override fun ComposableFapUninstallButton(modifier: Modifier, fapItem: FapItem) {
        ComposableFapUninstallButton(
            modifier = modifier,
            applicationUid = fapItem.id,
            dialogAppBox = {
                ComposableAppDialogBox(
                    fapItem = fapItem,
                    modifier = it
                )
            }
        )
    }

    @Composable
    override fun ComposableFapUninstallButton(modifier: Modifier, fapItem: FapItemShort) {
        ComposableFapUninstallButton(
            modifier = modifier,
            applicationUid = fapItem.id,
            dialogAppBox = {
                ComposableAppDialogBox(
                    fapItem = fapItem,
                    modifier = it
                )
            }
        )
    }
}
