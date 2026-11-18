package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model

enum class InstalledNetworkErrorEnum {
    GENERAL
}

fun Throwable.toInstalledNetworkErrorEnum(): InstalledNetworkErrorEnum {
    return InstalledNetworkErrorEnum.GENERAL
}
