package dev.namelessnanashi.flipperzero.archive.category.viewmodels

import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.DeleteKeyApi
import dev.namelessnanashi.flipperzero.core.ktx.jre.forEachIterable
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import dev.zacsweers.metro.Inject

class DeleteViewModel @Inject constructor(
    private val deleteKeyApi: DeleteKeyApi
) : DecomposeViewModel() {
    fun onDeleteAll() {
        viewModelScope.launch {
            val deletedKeys = deleteKeyApi.getDeletedKeyAsFlow().first()
            deletedKeys.forEachIterable {
                deleteKeyApi.deleteMarkedDeleted(it.path)
            }
        }
    }

    fun onRestoreAll() {
        viewModelScope.launch {
            val deletedKeys = deleteKeyApi.getDeletedKeyAsFlow().first()
            deletedKeys.forEachIterable {
                deleteKeyApi.restore(it.path)
            }
        }
    }
}
