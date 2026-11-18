package dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel

import android.app.Application
import android.os.Vibrator
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.core.ktx.android.vibrateCompat
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import com.flipperdevices.protobuf.screen.InputType
import dev.namelessnanashi.flipperzero.screenstreaming.impl.composable.ButtonEnum
import dev.namelessnanashi.flipperzero.screenstreaming.impl.model.FlipperScreenState
import dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel.repository.ButtonStackRepository
import dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel.repository.FlipperButtonRepository
import dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel.repository.LockRepository
import dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel.repository.StreamingRepository
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.StateFlow
import dev.namelessnanashi.flipperzero.bridge.connection.feature.screenstreaming.api.FScreenStreamingFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val VIBRATOR_TIME_MS = 10L

class ScreenStreamingViewModel @AssistedInject constructor(
    @Assisted private val lifecycleOwner: LifecycleOwner,
    application: Application,
    private val flipperButtonRepository: FlipperButtonRepository,
    private val buttonStackRepository: ButtonStackRepository,
    private val settings: DataStore<Settings>,
    private val fFeatureProvider: FFeatureProvider
) : DecomposeViewModel() {
    private val vibrator = ContextCompat.getSystemService(application, Vibrator::class.java)

    private val lockRepository = LockRepository(
        scope = viewModelScope,
        stackRepository = buttonStackRepository,
        fFeatureProvider = fFeatureProvider
    )
    private val streamingRepository = StreamingRepository(
        scope = viewModelScope,
        fFeatureProvider = fFeatureProvider
    )

    init {
        lifecycleOwner.lifecycle.subscribe(streamingRepository)
    }

    fun getFlipperScreen(): StateFlow<FlipperScreenState> = streamingRepository.getFlipperScreen()
    fun getFlipperButtons() = buttonStackRepository.getButtonStack()
    fun getLockState() = lockRepository.getLockState()
    fun onChangeLock(isWillBeLocked: Boolean) = lockRepository.onChangeLock(isWillBeLocked)
    fun onPressButton(
        buttonEnum: ButtonEnum,
        inputType: InputType
    ) {
        vibrator?.vibrateCompat(
            VIBRATOR_TIME_MS,
            runBlocking { settings.data.first().disabled_vibration }
        )

        val uuid = buttonStackRepository.onNewStackButton(buttonEnum.animEnum)
        flipperButtonRepository.pressOnButton(
            viewModelScope = viewModelScope,
            key = buttonEnum.key,
            type = inputType,
            onComplete = {
                buttonStackRepository.onRemoveStackButton(uuid)
            }
        )
    }

    private val keyboardErrorFlow = MutableStateFlow(false)

    fun getKeyboardError(): StateFlow<Boolean> = keyboardErrorFlow

    fun onDismissKeyboardError() {
        keyboardErrorFlow.value = false
    }

    /**
     * Types [text] on the Flipper (Momentum ASCII events)
     */
    fun onSendText(text: String) {
        if (text.isEmpty()) return
        viewModelScope.launch {
            val result = fFeatureProvider.getSync<FScreenStreamingFeatureApi>()?.sendAscii(text)
            if (result == null || result.isFailure) {
                keyboardErrorFlow.value = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleOwner.lifecycle.unsubscribe(streamingRepository)
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            lifecycleOwner: LifecycleOwner
        ): ScreenStreamingViewModel
    }
}
