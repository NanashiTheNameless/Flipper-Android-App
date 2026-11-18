package ${packageName}.impl.api

<#if isSingleScreenNavigation>
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dev.namelessnanashi.flipperzero.${__moduleName}.impl.composable.Composable${__formattedModuleName}
import dev.namelessnanashi.flipperzero.${__moduleName}.api.${__formattedModuleName}FeatureEntry
import dev.namelessnanashi.flipperzero.${__moduleName}.impl.viewmodel.${__formattedModuleName}ViewModel
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.navigation.ComposableFeatureEntry
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.ContributesMultibinding
import tangle.viewmodel.compose.tangleViewModel
import javax.inject.Inject

@ContributesBinding(AppGraph::class, ${__formattedModuleName}FeatureEntry::class)
@ContributesMultibinding(AppGraph::class, ComposableFeatureEntry::class)
class ${__formattedModuleName}FeatureEntryImpl @Inject constructor() : ${__formattedModuleName}FeatureEntry {
    override fun start(): String // TODO

    override fun NavGraphBuilder.composable(navController: NavHostController) {
        composable(start()) {
            val viewModel = tangleViewModel<${__formattedModuleName}ViewModel>()
            Composable${__formattedModuleName}()
        }
    }
}
<#else>
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dev.namelessnanashi.flipperzero.${__moduleName}.impl.composable.Composable${__formattedModuleName}
import dev.namelessnanashi.flipperzero.${__moduleName}.api.${__formattedModuleName}FeatureEntry
import dev.namelessnanashi.flipperzero.${__moduleName}.impl.viewmodel.${__formattedModuleName}ViewModel
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.navigation.AggregateFeatureEntry
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import tangle.viewmodel.compose.tangleViewModel

@ContributesBinding(AppGraph::class, ${__formattedModuleName}FeatureEntry::class)
@ContributesMultibinding(AppGraph::class, AggregateFeatureEntry::class)
class ${__formattedModuleName}FeatureEntryImpl @Inject constructor() : ${__formattedModuleName}FeatureEntry {
    override fun start(): String // TODO

    override fun NavGraphBuilder.navigation(navController: NavHostController) {
        navigation(startDestination = start(), route = ROUTE.name) {
            composable(start()) {
                val viewModel = tangleViewModel<${__formattedModuleName}ViewModel>()
                Composable${__formattedModuleName}()
            }
        }
    }
}
</#if>