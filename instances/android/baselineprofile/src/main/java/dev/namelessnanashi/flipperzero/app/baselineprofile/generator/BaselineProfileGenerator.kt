package dev.namelessnanashi.flipperzero.app.baselineprofile.generator

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dev.namelessnanashi.flipperzero.app.baselineprofile.scope.FirstLaunchIterationScope
import dev.namelessnanashi.flipperzero.app.baselineprofile.util.UiAutomatorExt
import dev.namelessnanashi.flipperzero.app.baselineprofile.util.UiAutomatorExt.clearCache
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * THIS WILL WIPE YOUR FLIPPER ANDROID INSTALL
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@RequiresApi(Build.VERSION_CODES.P)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = UiAutomatorExt.requirePackageName,
            maxIterations = 10,
            stableIterations = 3
        ) {
            pressHome()
            startActivityAndWait()
            with(FirstLaunchIterationScope) { tryRun() }
            device.clearCache()
        }
    }
}
