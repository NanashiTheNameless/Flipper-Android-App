package dev.namelessnanashi.flipperzero.firstpair.impl.storage

interface FirstPairStorage {
    fun isDeviceSelected(): Boolean
    fun markDeviceSelected(deviceId: String?, deviceName: String?, address: String?)
}
