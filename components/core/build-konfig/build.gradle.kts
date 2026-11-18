import dev.namelessnanashi.flipperzero.buildlogic.ApkConfig
import dev.namelessnanashi.flipperzero.buildlogic.ApkConfig.CURRENT_FLAVOR_TYPE
import dev.namelessnanashi.flipperzero.buildlogic.ApkConfig.TARGET_APP_ID_KEY
import dev.namelessnanashi.flipperzero.buildlogic.ApkConfig.VERSION_NAME
import dev.detekt.gradle.Detekt
import org.gradle.kotlin.dsl.buildConfigField
import org.gradle.kotlin.dsl.withType

plugins {
    id("flipper.multiplatform")
    id("flipper.multiplatform-dependencies")
    alias(libs.plugins.buildkonfig)
}

group = "dev.namelessnanashi.flipperzero.core.buildkonfig"

buildConfig {
    className("BuildKonfig")
    packageName("$group")
    useKotlinOutput { internalVisibility = false }
    buildConfigField(String::class.java, "PACKAGE", "$group")
    buildConfigField(Boolean::class.java, "IS_LOG_ENABLED", CURRENT_FLAVOR_TYPE.isLogEnabled)
    buildConfigField(
        Boolean::class.java,
        "IS_VERBOSE_LOG_ENABLED",
        CURRENT_FLAVOR_TYPE.isVerboseLogEnabled
    )
    buildConfigField(String::class.java, "TARGET_APP_ID_KEY", TARGET_APP_ID_KEY)
    buildConfigField(String::class.java, "VERSION_NAME", VERSION_NAME)
    buildConfigField(String::class.java, "BUILD_TYPE", CURRENT_FLAVOR_TYPE.buildTypeName)
    buildConfigField(
        Boolean::class.java,
        "CRASH_APP_ON_FAILED_CHECKS",
        CURRENT_FLAVOR_TYPE.crashAppOnFailedChecks
    )
    buildConfigField(
        Boolean::class.java,
        "LOGGING_PENDING_COMMANDS",
        CURRENT_FLAVOR_TYPE.loggingPendingCommands
    )
    buildConfigField(
        Integer::class.java,
        "ROBOELECTRIC_SDK_VERSION",
        ApkConfig.ROBOELECTRIC_SDK_VERSION
    )
}

tasks.withType<Detekt> {
    enabled = false
}
