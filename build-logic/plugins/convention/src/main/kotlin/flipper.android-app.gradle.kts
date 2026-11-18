import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import dev.namelessnanashi.flipperzero.buildlogic.ApkConfig
import dev.namelessnanashi.flipperzero.buildlogic.BuildVersionCode
import java.util.Properties

plugins {
    id("com.android.application")
    id("flipper.lint")
    id("org.jetbrains.kotlin.plugin.compose")
}

pluginManager.apply("org.jetbrains.kotlin.android")

/**
 * Release signing: environment first (CI), then a properties file next to the repo
 * (never inside it). Missing values build an unsigned release.
 */
val localSigning = Properties().apply {
    val text = providers.fileContents(
        isolated.rootProject.projectDirectory.file("../flipper-nanashi-keystore.properties")
    ).asText.orNull
    if (text != null) load(text.reader())
}

fun signingValue(envName: String, propertyKey: String): String? =
    providers.environmentVariable(envName).orNull?.takeIf { it.isNotBlank() }
        ?: localSigning.getProperty(propertyKey)?.takeIf { it.isNotBlank() }

val releaseStoreFile = signingValue("KEYSTORE_FILE", "storeFile")?.let { file(it) }
val releaseStorePassword = signingValue("KEYSTORE_PASSWORD", "storePassword")
val releaseKeyAlias = signingValue("KEY_ALIAS", "keyAlias")
val releaseKeyPassword = signingValue("KEY_PASSWORD", "keyPassword")
val hasReleaseSigning = releaseStoreFile?.isFile == true &&
    releaseStorePassword != null && releaseKeyAlias != null && releaseKeyPassword != null

@Suppress("UnstableApiUsage")
configure<ApplicationExtension> {
    commonAndroid(project)

    defaultConfig {
        applicationId = ApkConfig.APPLICATION_ID
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                enableV1Signing = false
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        debug {
            // Lets the debug build sit beside a release install
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        internal {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

val buildVersionCode = providers.of(BuildVersionCode::class) {}

configure<ApplicationAndroidComponentsExtension> {
    onVariants { variant ->
        variant.outputs.forEach { it.versionCode.set(buildVersionCode) }
    }
}

includeCommonKspConfigurationTo("ksp")
