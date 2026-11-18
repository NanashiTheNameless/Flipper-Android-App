package dev.namelessnanashi.flipperzero.bridge.connection.feature.seriallagsdetector.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.actionnotifier.api.FlipperActionNotifier
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, FlipperActionNotifier.Factory::class)
class FlipperActionNotifierImpl @AssistedInject constructor(
    @Assisted private val scope: CoroutineScope
) : FlipperActionNotifier {
    private val actionFlow = MutableSharedFlow<Unit>()

    override fun getActionFlow(): Flow<Unit> = actionFlow

    override fun notifyAboutAction() {
        scope.launch(FlipperDispatchers.workStealingDispatcher) {
            actionFlow.emit(Unit)
        }
    }
}
