package dev.namelessnanashi.flipperzero.updater.card.helpers

import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.updater.model.DistributionFile
import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion
import dev.namelessnanashi.flipperzero.updater.model.OfficialFirmware
import dev.namelessnanashi.flipperzero.updater.model.UpdateCardState
import dev.namelessnanashi.flipperzero.updater.model.UpdateErrorType
import dev.namelessnanashi.flipperzero.updater.model.VersionFiles
import dev.namelessnanashi.flipperzero.updater.model.WebUpdaterFirmware
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException
import java.util.EnumMap

class UpdateCardHelperTest {

    private val versionMap: EnumMap<FirmwareChannel, VersionFiles> = EnumMap(
        FirmwareChannel::class.java
    )

    @Before
    fun setup() {
        versionMap[FirmwareChannel.DEV] = VersionFiles(
            version = FirmwareVersion(
                channel = FirmwareChannel.DEV,
                version = "1.0.0"
            ),
            updaterFile = DistributionFile(url = "", sha256 = "")
        )
    }

    @Test
    fun updateCardStateInProgress() = runTest {
        val helper = UpdateCardHelper(
            updateChannel = null,
            isFlashExist = null,
            firmwareVersion = null,
            alwaysShowUpdate = false,
            webUpdate = null,
            latestVersionAsync = mockk()
        )
        val state = helper.processUpdateCardState()
        Assert.assertTrue(state is UpdateCardState.InProgress)
    }

    @Test
    fun updateCardStateNoSDFlash() = runTest {
        val helper = UpdateCardHelper(
            updateChannel = null,
            isFlashExist = false,
            firmwareVersion = FirmwareVersion(channel = mockk(), version = ""),
            alwaysShowUpdate = false,
            webUpdate = null,
            latestVersionAsync = mockk()
        )
        val state = helper.processUpdateCardState()
        Assert.assertTrue(state is UpdateCardState.Error)
        Assert.assertEquals((state as UpdateCardState.Error).type, UpdateErrorType.NO_SD_CARD)
    }

    @Test
    fun updateCardStateNetworkException() = runTest {
        val latestVersionAsync = async {
            return@async runCatching { throw UnknownHostException("") }
        }
        val helper = UpdateCardHelper(
            updateChannel = null,
            isFlashExist = true,
            firmwareVersion = FirmwareVersion(channel = mockk(), version = ""),
            alwaysShowUpdate = false,
            webUpdate = null,
            latestVersionAsync = latestVersionAsync
        )
        val state = helper.processUpdateCardState()
        Assert.assertTrue(state is UpdateCardState.Error)
        Assert.assertEquals((state as UpdateCardState.Error).type, UpdateErrorType.NO_INTERNET)
    }

    @Test
    fun updateCardStateServerException() = runTest {
        val latestVersionAsync = async {
            return@async runCatching {
                @Suppress("TooGenericExceptionThrown")
                throw Exception("")
            }
        }
        val helper = UpdateCardHelper(
            updateChannel = null,
            isFlashExist = true,
            firmwareVersion = FirmwareVersion(channel = mockk(), version = ""),
            alwaysShowUpdate = false,
            webUpdate = null,
            latestVersionAsync = latestVersionAsync
        )
        val state = helper.processUpdateCardState()
        Assert.assertTrue(state is UpdateCardState.Error)
        Assert.assertEquals((state as UpdateCardState.Error).type, UpdateErrorType.UNABLE_TO_SERVER)
    }

    @Test
    fun updateCardStateWebUpdate() = runTest {
        val latestVersionAsync = async {
            return@async runCatching { versionMap }
        }
        val helper = UpdateCardHelper(
            updateChannel = null,
            isFlashExist = true,
            firmwareVersion = FirmwareVersion(channel = FirmwareChannel.CUSTOM, version = "1"),
            alwaysShowUpdate = false,
            webUpdate = Deeplink.BottomBar.DeviceTab.WebUpdate(
                name = "123 test",
                url = "456"
            ),
            latestVersionAsync = latestVersionAsync
        )
        val state = helper.processUpdateCardState()
        Assert.assertTrue(state is UpdateCardState.UpdateAvailable)
        Assert.assertEquals((state as UpdateCardState.UpdateAvailable).isOtherChannel, true)
        Assert.assertEquals(
            state.update.updateFrom,
            FirmwareVersion(channel = FirmwareChannel.CUSTOM, version = "1")
        )
        Assert.assertEquals(
            state.update.updateTo,
            FirmwareVersion(channel = FirmwareChannel.CUSTOM, version = "123")
        )
        Assert.assertEquals(
            state.update.changelog,
            null
        )
        Assert.assertTrue(state.update.content is WebUpdaterFirmware)
        Assert.assertEquals((state.update.content as WebUpdaterFirmware).url, "456")
    }

    @Test
    fun updateCardStateFileUpdate() = runTest {
        val latestVersionAsync = async {
            return@async runCatching { versionMap }
        }
        val helper = UpdateCardHelper(
            updateChannel = FirmwareChannel.CUSTOM,
            isFlashExist = true,
            firmwareVersion = FirmwareVersion(channel = FirmwareChannel.CUSTOM, version = "1"),
            alwaysShowUpdate = false,
            webUpdate = null,
            latestVersionAsync = latestVersionAsync
        )
        val state = helper.processUpdateCardState()
        Assert.assertTrue(state is UpdateCardState.UpdateFromFile)
        Assert.assertEquals(
            (state as UpdateCardState.UpdateFromFile).flipperVersion,
            FirmwareVersion(channel = FirmwareChannel.CUSTOM, version = "1")
        )
        Assert.assertEquals(
            state.updateVersion,
            FirmwareVersion(channel = FirmwareChannel.CUSTOM, version = "")
        )
    }

    @Test
    fun updateCardStateNoUpdate() = runTest {
        val latestVersionAsync = async {
            return@async runCatching { versionMap }
        }
        val helper = UpdateCardHelper(
            updateChannel = null,
            isFlashExist = true,
            firmwareVersion = FirmwareVersion(channel = FirmwareChannel.CUSTOM, version = "1"),
            alwaysShowUpdate = false,
            webUpdate = null,
            latestVersionAsync = latestVersionAsync
        )
        val state = helper.processUpdateCardState()
        Assert.assertTrue(state is UpdateCardState.NoUpdate)
        Assert.assertEquals(
            (state as UpdateCardState.NoUpdate).flipperVersion,
            FirmwareVersion(channel = FirmwareChannel.CUSTOM, version = "1")
        )
    }

    @Test
    fun updateCardStateUpdateAvailableAlwaysUpdate() = runTest {
        val latestVersionAsync = async {
            return@async runCatching { versionMap }
        }
        val helper = UpdateCardHelper(
            updateChannel = FirmwareChannel.DEV,
            isFlashExist = true,
            firmwareVersion = FirmwareVersion(channel = FirmwareChannel.CUSTOM, version = "1"),
            alwaysShowUpdate = true,
            webUpdate = null,
            latestVersionAsync = latestVersionAsync
        )
        val state = helper.processUpdateCardState()
        Assert.assertTrue(state is UpdateCardState.UpdateAvailable)
        Assert.assertEquals((state as UpdateCardState.UpdateAvailable).isOtherChannel, true)
        Assert.assertEquals(
            state.update.updateFrom,
            FirmwareVersion(channel = FirmwareChannel.CUSTOM, version = "1")
        )
        Assert.assertEquals(
            state.update.updateTo,
            FirmwareVersion(channel = FirmwareChannel.DEV, version = "1.0.0")
        )
        Assert.assertEquals(
            state.update.changelog,
            null
        )
        Assert.assertTrue(state.update.content is OfficialFirmware)
        Assert.assertEquals((state.update.content as OfficialFirmware).distributionFile.url, "")
        Assert.assertEquals(
            (state.update.content as OfficialFirmware).distributionFile.sha256,
            ""
        )
    }

    @Test
    fun updateCardStateUpdateNotAvailable() = runTest {
        val latestVersionAsync = async {
            return@async runCatching { versionMap }
        }
        val helper = UpdateCardHelper(
            updateChannel = FirmwareChannel.DEV,
            isFlashExist = true,
            firmwareVersion = FirmwareVersion(channel = FirmwareChannel.DEV, version = "1.0.0"),
            alwaysShowUpdate = false,
            webUpdate = null,
            latestVersionAsync = latestVersionAsync
        )
        val state = helper.processUpdateCardState()
        Assert.assertTrue(state is UpdateCardState.NoUpdate)
    }
}
