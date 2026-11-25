package dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGetInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.model.FGetInfoApiGroup
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.meta.FTransportMetaInfoApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.meta.TransportMetaInfoKey
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList

private const val POWER_POLL_INTERVAL_MS = 30_000L
private const val RETRY_DELAY_MS = 2_000L
private const val MANUFACTURER = "Flipper Devices Inc."
private const val CHARGING_STATE = "charging"

// Same bits the GATT battery power state characteristic sets while charging
private const val CHARGING_POWER_STATE: Byte = 0b0011_0000

/**
 * Fork: the information the BLE GATT services provide (name, firmware, battery), read over RPC
 * for transports that have no GATT (USB). Upstream's USB transport made these values up
 * ("1.0.0 fakeusb0", 0 % battery, which also blocked firmware updates).
 *
 * Device info is read once; power info every [POWER_POLL_INTERVAL_MS] while collected.
 */
internal class RpcGattMetaInfoApi(
    private val getInfoApi: FGetInfoFeatureApi,
    scope: CoroutineScope
) : FTransportMetaInfoApi, LogTagProvider {
    override val TAG = "RpcGattMetaInfoApi"

    private val deviceInfo: StateFlow<Map<String, String>?> = flow {
        while (true) {
            val devInfo = readGroup(FGetInfoApiGroup.DEVICE_INFO)
            if (devInfo != null) {
                info {
                    "Firmware: " + devInfo.filterKeys { it.startsWith("firmware.") }
                        .entries.joinToString { (key, value) -> "$key=$value" }
                }
                emit(devInfo)
                return@flow
            }
            delay(RETRY_DELAY_MS)
        }
    }.stateIn(scope, SharingStarted.Lazily, null)

    private val powerInfo: StateFlow<Map<String, String>?> = flow {
        while (true) {
            readGroup(FGetInfoApiGroup.POWER_INFO)?.let { emit(it) }
            delay(POWER_POLL_INTERVAL_MS)
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(), null)

    private suspend fun readGroup(group: FGetInfoApiGroup): Map<String, String>? = runCatching {
        getInfoApi.get(group).toList().associate { (property, value) ->
            // Keys come without the group prefix; strip it in case a firmware adds one
            property.key.removePrefix("${group.key}.") to value
        }
    }.onFailure { error(it) { "Could not read ${group.key}" } }
        .getOrNull()
        ?.takeIf { it.isNotEmpty() }

    override fun get(key: TransportMetaInfoKey): Result<Flow<ByteArray?>> {
        val flow = when (key) {
            TransportMetaInfoKey.DEVICE_NAME -> deviceInfo.value(DEVICE_NAME)
            TransportMetaInfoKey.MANUFACTURER -> deviceInfo.map { info -> info?.let { MANUFACTURER } }
            TransportMetaInfoKey.HARDWARE_VERSION -> deviceInfo.value(HARDWARE_VERSION)
            TransportMetaInfoKey.SOFTWARE_VERSION -> deviceInfo.map { info ->
                info?.let(::softwareRevision)
            }
            TransportMetaInfoKey.API_VERSION -> deviceInfo.map { info ->
                val major = info?.get(PROTOBUF_MAJOR) ?: return@map null
                val minor = info[PROTOBUF_MINOR] ?: return@map null
                "$major.$minor"
            }
            TransportMetaInfoKey.BATTERY_LEVEL -> return Result.success(
                powerInfo.map { info ->
                    info?.get(CHARGE_LEVEL)?.toIntOrNull()?.coerceIn(0, 100)?.let { byteArrayOf(it.toByte()) }
                }.distinctUntilChanged { old, new -> old.contentEquals(new) }
            )
            TransportMetaInfoKey.BATTERY_POWER_STATE -> return Result.success(
                powerInfo.map { info ->
                    val state = info?.get(CHARGE_STATE) ?: return@map null
                    byteArrayOf(if (state == CHARGING_STATE) CHARGING_POWER_STATE else 0)
                }.distinctUntilChanged { old, new -> old.contentEquals(new) }
            )
        }
        return Result.success(flow.map { it?.toByteArray() })
    }

    private fun StateFlow<Map<String, String>?>.value(infoKey: String) = map { it?.get(infoKey) }

    /**
     * The GATT software revision string: "<commit> <version> <branch number> <build date>",
     * which `FirmwareVersionBuilderApi` parses. Its second part must be exactly "dev" for a dev
     * build, or the app shows the version name instead of the commit hash. Over RPC a dev build
     * may say so in the branch ("dev") or the version ("mntm-dev"), so either one counts.
     */
    private fun softwareRevision(info: Map<String, String>): String? {
        val commit = info[FIRMWARE_COMMIT] ?: return null
        val rawVersion = info[FIRMWARE_VERSION]?.takeIf { it.isNotBlank() }
        val branch = info[FIRMWARE_BRANCH]?.takeIf { it.isNotBlank() }
        val isDev = listOfNotNull(rawVersion, branch).any { part ->
            part.equals(DEV, ignoreCase = true) || part.endsWith("-$DEV", ignoreCase = true)
        }
        val version = (if (isDev) DEV else rawVersion ?: branch) ?: return null
        val branchNum = info[FIRMWARE_BRANCH_NUM] ?: "0"
        val date = info[FIRMWARE_BUILD_DATE] ?: return null
        return "$commit $version $branchNum $date"
    }

    companion object {
        private const val DEV = "dev"
        private const val DEVICE_NAME = "hardware.name"
        private const val HARDWARE_VERSION = "hardware.ver"
        private const val FIRMWARE_COMMIT = "firmware.commit.hash"
        private const val FIRMWARE_VERSION = "firmware.version"
        private const val FIRMWARE_BRANCH = "firmware.branch.name"
        private const val FIRMWARE_BRANCH_NUM = "firmware.branch.num"
        private const val FIRMWARE_BUILD_DATE = "firmware.build.date"
        private const val PROTOBUF_MAJOR = "protobuf.version.major"
        private const val PROTOBUF_MINOR = "protobuf.version.minor"
        private const val CHARGE_LEVEL = "charge.level"
        private const val CHARGE_STATE = "charge.state"
    }
}
