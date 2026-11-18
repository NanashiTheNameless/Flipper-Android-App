package dev.namelessnanashi.flipperzero.faphub.report.impl.viewmodel

import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapReportApi
import dev.namelessnanashi.flipperzero.faphub.report.impl.model.FapReportState
import dev.namelessnanashi.flipperzero.inappnotification.api.InAppNotificationStorage
import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportViewModel @AssistedInject constructor(
    @Assisted private val applicationUid: String,
    private val reportApi: FapReportApi,
    private val inAppNotificationStorage: InAppNotificationStorage
) : DecomposeViewModel(), LogTagProvider {
    override val TAG = "ReportViewModel"

    private val state = MutableStateFlow<FapReportState>(FapReportState.ReadyToReport)

    fun getFapReportState() = state.asStateFlow()

    fun submit(onBack: () -> Unit, text: String) {
        if (!state.compareAndSet(FapReportState.ReadyToReport, FapReportState.Uploading)) {
            return
        }
        viewModelScope.launch {
            try {
                reportApi.report(applicationUid, text)
                inAppNotificationStorage.addNotification(InAppNotification.ReportApp)
            } catch (e: Exception) {
                error(e) { "Failed report bug" }
            }
            withContext(Dispatchers.Main) {
                onBack()
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            applicationUid: String
        ): ReportViewModel
    }
}
