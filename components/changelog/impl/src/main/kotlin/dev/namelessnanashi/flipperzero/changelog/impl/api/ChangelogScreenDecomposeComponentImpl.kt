package dev.namelessnanashi.flipperzero.changelog.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.changelog.api.ChangelogFormatterApi
import dev.namelessnanashi.flipperzero.changelog.api.ChangelogScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.changelog.impl.composable.ChangelogScreenComposable
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.statusbar.ThemeStatusBarIconStyleProvider
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, ChangelogScreenDecomposeComponent.Factory::class)
class ChangelogScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val updateRequest: UpdateRequest,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val changelogFormatter: ChangelogFormatterApi,
    dataStore: DataStore<Settings>
) : ChangelogScreenDecomposeComponent(componentContext) {

    private val themeStatusBarIconStyleProvider = ThemeStatusBarIconStyleProvider(dataStore)

    @Composable
    override fun Render() {
        val changelog = updateRequest.changelog
        ChangelogScreenComposable(
            updateRequest = updateRequest,
            changelog = remember(changelog) {
                if (changelog != null) {
                    changelogFormatter.format(changelog)
                } else {
                    null
                }
            },
            onBack = onBack::invoke
        )
    }

    override fun isStatusBarIconLight(systemIsDark: Boolean): Boolean {
        return themeStatusBarIconStyleProvider.isStatusBarIconLight(systemIsDark)
    }
}
