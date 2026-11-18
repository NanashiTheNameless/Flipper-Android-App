package dev.namelessnanashi.flipperzero.singleactivity.impl.di

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.singleactivity.impl.SingleActivity
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppGraph::class)
interface SingleActivityComponent {
    fun inject(activity: SingleActivity)
}
