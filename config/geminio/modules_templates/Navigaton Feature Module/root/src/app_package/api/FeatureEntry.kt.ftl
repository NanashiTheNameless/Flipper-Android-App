package ${packageName}.api

<#if isSingleScreenNavigation>
import dev.namelessnanashi.flipperzero.core.ui.navigation.ComposableFeatureEntry
import dev.namelessnanashi.flipperzero.core.ui.navigation.FeatureScreenRootRoute

interface ${__formattedModuleName}FeatureEntry : ComposableFeatureEntry {
    override val ROUTE: FeatureScreenRootRoute
        get() = FeatureScreenRootRoute.

    fun start(): String
}

<#else>
import dev.namelessnanashi.flipperzero.core.ui.navigation.AggregateFeatureEntry
import dev.namelessnanashi.flipperzero.core.ui.navigation.FeatureScreenRootRoute

interface ${__formattedModuleName}FeatureEntry : AggregateFeatureEntry {
    override val ROUTE: FeatureScreenRootRoute
        get() = FeatureScreenRootRoute.

    fun start(): String
}
</#if>
