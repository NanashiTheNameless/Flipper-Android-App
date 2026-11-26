package dev.namelessnanashi.flipperzero.toolstab.impl.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.ui.ktx.OrangeAppBar
import dev.namelessnanashi.flipperzero.toolstab.impl.R
import dev.namelessnanashi.flipperzero.toolstab.impl.composable.elements.MifareClassicComposable
import dev.namelessnanashi.flipperzero.toolstab.impl.composable.elements.RemoteControlsComposable

@Composable
fun ComposableHub(
    hasNotification: Boolean,
    onOpenMfKey32: () -> Unit,
    onOpenRemoteControls: () -> Unit,
    appSourcesEntry: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        OrangeAppBar(
            titleId = R.string.toolstab_title
        )
        MifareClassicComposable(
            hasMfKey32Notification = hasNotification,
            onOpenMfKey32 = onOpenMfKey32
        )
        RemoteControlsComposable(
            onOpenRemoteControls = onOpenRemoteControls
        )
        appSourcesEntry()
    }
}
