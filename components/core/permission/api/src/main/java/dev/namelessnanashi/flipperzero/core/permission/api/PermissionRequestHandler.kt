package dev.namelessnanashi.flipperzero.core.permission.api

typealias PermissionListener = (String, Boolean) -> Unit

interface PermissionRequestHandler {
    fun requestPermission(vararg permissions: String, listener: PermissionListener)
}
