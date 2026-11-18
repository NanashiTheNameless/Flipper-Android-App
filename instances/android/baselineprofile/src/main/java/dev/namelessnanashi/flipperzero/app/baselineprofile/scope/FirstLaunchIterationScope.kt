package dev.namelessnanashi.flipperzero.app.baselineprofile.scope

import android.Manifest
import android.os.Build
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import dev.namelessnanashi.flipperzero.app.baselineprofile.util.UiAutomatorExt.grantPermission
import dev.namelessnanashi.flipperzero.app.baselineprofile.util.UiAutomatorExt.waitForObjectOrNull
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info

object FirstLaunchIterationScope : LogTagProvider {
    override val TAG = "FirstLaunchIterationScope"

    private fun MacrobenchmarkScope.connectDevice() {
        info { "#connectDevice" }
        val connectButtonSelector = By.text("Connect")
        val connectButton = device.waitForObjectOrNull(connectButtonSelector)
        connectButton?.click()
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.skipConnection() {
        info { "#skipConnection" }
        val skipConnectionSelector = By.text("Skip Connection")
        val skipConnectionButton = device.waitForObjectOrNull(skipConnectionSelector)
        skipConnectionButton?.click()
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.grantRequiredPermissions() {
        info { "#grantRequiredPermissions" }
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }.forEach { permission -> device.grantPermission(permission) }
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.skipNotification() {
        info { "#skipNotification" }
        val skipNotificationSelector = By.text("Skip")
        val skipNotificationButton = device.waitForObjectOrNull(skipNotificationSelector)
        skipNotificationButton?.click()
        device.waitForIdle()
    }

    fun MacrobenchmarkScope.tryRun() {
        info { "#tryRun" }
        grantRequiredPermissions()
        connectDevice()
        skipConnection()
        skipNotification()
    }
}
