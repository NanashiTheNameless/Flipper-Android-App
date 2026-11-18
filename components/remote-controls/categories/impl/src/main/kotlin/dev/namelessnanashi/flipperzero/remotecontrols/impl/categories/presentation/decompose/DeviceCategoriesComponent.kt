package dev.namelessnanashi.flipperzero.remotecontrols.impl.categories.presentation.decompose

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FapHubError
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.DeviceCategory
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

interface DeviceCategoriesComponent {
    val model: StateFlow<Model>

    fun onCategoryClick(category: DeviceCategory)

    fun onBackClick()

    fun tryLoad()

    sealed interface Model {
        data object Loading : Model
        class Loaded(val deviceTypes: ImmutableList<DeviceCategory>) : Model
        data class Error(val throwable: FapHubError) : Model
    }

    fun interface Factory {
        fun invoke(
            componentContext: ComponentContext,
            onBackClick: DecomposeOnBackParameter,
            onCategoryClick: (categoryId: Long, categoryName: String) -> Unit
        ): DeviceCategoriesComponent
    }
}
