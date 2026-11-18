package dev.namelessnanashi.flipperzero.buildlogic

import dev.namelessnanashi.flipperzero.buildlogic.model.FlavorType
import org.gradle.api.Project

object ApkConfig {
    const val APPLICATION_ID = "dev.namelessnanashi.flipperzero"

    const val MIN_SDK_VERSION = 26

    const val TARGET_SDK_VERSION = 36
    const val COMPILE_SDK_VERSION = 37
    const val ROBOELECTRIC_SDK_VERSION = 34
    const val TARGET_APP_ID_KEY = "targetAppId"

    // Plain MAJOR.MINOR.PATCH, for packagers that reject suffixes (desktop dmg/msi)
    const val BASE_VERSION = "1.8.1"

    // CI passes -PversionSuffix=<sha7>; local builds stay "localbuild".
    // versionCode is time based, see BuildVersionCode.
    val Project.VERSION_NAME
        get() = "$BASE_VERSION-" + (propOrNull("versionSuffix")?.takeIf { it.isNotBlank() } ?: "localbuild")

    val Project.CURRENT_FLAVOR_TYPE: FlavorType
        get() {
            val default = FlavorType.DEV
            val key = "current_flavor_type"
            val propValue = propOrNull(key)
            if (propValue == null) {
                logger.warn("Property $key was not found, writing default $default")
            }
            return FlavorType.entries.find { it.name == propValue } ?: default
        }
}

internal fun Project.propOrNull(key: String): String? {
    return providers.gradleProperty(key).orNull
}

internal fun Project.prop(key: String, default: Any): String {
    return providers.gradleProperty(key).getOrElse(default.toString())
}
