package dev.namelessnanashi.flipperzero.app.baselineprofile.scope

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import dev.namelessnanashi.flipperzero.app.baselineprofile.util.UiAutomatorExt.MEDIUM_TIMEOUT
import dev.namelessnanashi.flipperzero.app.baselineprofile.util.UiAutomatorExt.requirePackageName
import dev.namelessnanashi.flipperzero.app.baselineprofile.util.UiAutomatorExt.waitForObjectOrNull
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info

object TabIterationScope : LogTagProvider {
    override val TAG: String = "TabIterationScope"

    private fun MacrobenchmarkScope.openArchive() {
        info { "#openArchive" }
        val deviceInfoSelector = By.text("Archive")
        val archiveTabIcon = device.waitForObjectOrNull(deviceInfoSelector)
        archiveTabIcon?.click()
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.openHub() {
        info { "#openHub" }
        val deviceInfoSelector = By.text("Hub")
        val archiveTabIcon = device.waitForObjectOrNull(deviceInfoSelector)
        archiveTabIcon?.click()
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.openApps() {
        info { "#openApps" }
        device.waitForIdle()
        device.waitForWindowUpdate(requirePackageName, MEDIUM_TIMEOUT)
        val deviceInfoSelector = By.text("Apps")
        val appCard = device.waitForObjectOrNull(deviceInfoSelector)
        appCard?.click()
        device.waitForIdle()
        device.waitForWindowUpdate(requirePackageName, MEDIUM_TIMEOUT)
    }

    private fun MacrobenchmarkScope.waitForTabScreen() {
        info { "#waitForTabScreen" }
        val deviceInfoSelector = By.textContains("Device Info")
        device.waitForObjectOrNull(deviceInfoSelector)
        device.waitForIdle()
    }

    fun MacrobenchmarkScope.tryRun() {
        info { "#tryRun" }
        waitForTabScreen()
        openArchive()
        openHub()
        openApps()
    }
}
