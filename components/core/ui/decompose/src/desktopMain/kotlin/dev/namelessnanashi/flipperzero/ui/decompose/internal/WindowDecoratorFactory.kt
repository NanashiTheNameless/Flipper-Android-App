package dev.namelessnanashi.flipperzero.ui.decompose.internal

import dev.namelessnanashi.flipperzero.ui.decompose.statusbar.StatusBarIconStyleProvider

internal actual fun createWindowDecorator(
    statusBarIconStyleProvider: StatusBarIconStyleProvider
): WindowDecorator = DesktopWindowDecorator()
