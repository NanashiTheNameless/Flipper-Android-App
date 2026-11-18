package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model

import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem

sealed class FapInstalledInternalState(
    val order: Int
) : Comparable<FapInstalledInternalState> {
    data object InstallingInProgressActive : FapInstalledInternalState(order = 4)

    data object InstallingInProgress : FapInstalledInternalState(order = 3)
    data object UpdatingInProgressActive : FapInstalledInternalState(order = 4)

    data object UpdatingInProgress : FapInstalledInternalState(order = 3)

    data class ReadyToUpdate(
        val manifestItem: FapManifestItem
    ) : FapInstalledInternalState(order = 2)

    data object Installed : FapInstalledInternalState(order = 1)
    data object InstalledOffline : FapInstalledInternalState(order = 0)

    override fun compareTo(other: FapInstalledInternalState): Int {
        return other.order - this.order
    }
}
