package dev.namelessnanashi.flipperzero.bridge.connection.di

import dev.namelessnanashi.flipperzero.bridge.connection.ConnectionTestActivity

interface AppComponent {
    fun inject(activity: ConnectionTestActivity)
}
