package dev.namelessnanashi.flipperzero.faphub.category.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory
import dev.namelessnanashi.flipperzero.ui.decompose.CompositeDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import uk.kulikov.metro.assisted.AssistedKey

abstract class FapHubCategoryDecomposeComponent<C : Any> : CompositeDecomposeComponent<C>() {

    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @AssistedKey("fapCategory") category: FapCategory,
            onBack: DecomposeOnBackParameter,
        ): FapHubCategoryDecomposeComponent<*>
    }
}
