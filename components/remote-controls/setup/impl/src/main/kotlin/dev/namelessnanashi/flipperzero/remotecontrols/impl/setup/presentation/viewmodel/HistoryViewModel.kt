package dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.presentation.viewmodel

import dev.namelessnanashi.flipperzero.core.ktx.jre.safeRemoveLast
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.SignalModel
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.SignalRequestModel.SignalResultData
import dev.zacsweers.metro.Inject

class HistoryViewModel @Inject constructor() : DecomposeViewModel() {
    private val flatData = mutableListOf<FlatData>()

    val data: State
        get() = State(
            successfulSignals = flatData
                .filter { it.type == FlatData.FlatDataType.SUCCESS }
                .map(FlatData::data),
            failedSignals = flatData
                .filter { it.type == FlatData.FlatDataType.FAILED }
                .map(FlatData::data),
            skippedSignals = flatData
                .filter { it.type == FlatData.FlatDataType.SKIPPED }
                .map(FlatData::data),
        )
    val isEmpty: Boolean
        get() = flatData.isEmpty()

    fun forgetLast() {
        flatData.safeRemoveLast()
    }

    fun rememberSuccessful(signalModel: SignalModel) {
        val signalResultData = SignalResultData(
            signalId = signalModel.id,
        )
        flatData += FlatData(signalResultData, FlatData.FlatDataType.SUCCESS)
    }

    fun rememberFailed(signalModel: SignalModel) {
        val signalResultData = SignalResultData(
            signalId = signalModel.id,
        )
        flatData += FlatData(signalResultData, FlatData.FlatDataType.FAILED)
    }

    fun rememberSkipped(signalModel: SignalModel) {
        val signalResultData = SignalResultData(
            signalId = signalModel.id,
        )
        flatData += FlatData(signalResultData, FlatData.FlatDataType.SKIPPED)
    }

    data class State(
        val successfulSignals: List<SignalResultData> = emptyList(),
        val failedSignals: List<SignalResultData> = emptyList(),
        val skippedSignals: List<SignalResultData> = emptyList()
    )

    class FlatData(
        val data: SignalResultData,
        val type: FlatDataType
    ) {
        enum class FlatDataType {
            SUCCESS, FAILED, SKIPPED
        }
    }
}
