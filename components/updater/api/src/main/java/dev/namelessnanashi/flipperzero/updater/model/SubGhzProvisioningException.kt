package dev.namelessnanashi.flipperzero.updater.model

class SubGhzProvisioningException(
    val errorCode: Int,
    message: String
) : RuntimeException(message)
