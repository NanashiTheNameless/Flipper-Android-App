package dev.namelessnanashi.flipperzero.ui.decompose.internal

import dev.namelessnanashi.flipperzero.ui.decompose.statusbar.StatusBarIconStyleProvider

internal expect fun createWindowDecorator(
    statusBarIconStyleProvider: StatusBarIconStyleProvider
): WindowDecorator
