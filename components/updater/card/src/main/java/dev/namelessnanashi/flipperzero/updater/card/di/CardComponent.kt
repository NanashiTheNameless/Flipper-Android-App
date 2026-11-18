package dev.namelessnanashi.flipperzero.updater.card.di

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.updater.card.viewmodel.UpdateCardViewModel
import dev.namelessnanashi.flipperzero.updater.card.viewmodel.UpdateRequestViewModel
import dev.namelessnanashi.flipperzero.updater.card.viewmodel.UpdateStateViewModel
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppGraph::class)
interface CardComponent {
    fun inject(viewModel: UpdateStateViewModel)
    fun inject(viewModel: UpdateCardViewModel)
    fun inject(viewModel: UpdateRequestViewModel)
}
