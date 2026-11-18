package dev.namelessnanashi.flipperzero.faphub.dao.api.model

enum class FapBuildState {
    READY,
    READY_ON_RELEASE,
    BUILD_RUNNING,
    UNSUPPORTED_APP,
    FLIPPER_OUTDATED,
    UNSUPPORTED_SDK
}
