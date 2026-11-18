plugins {
    id("flipper.android-app")
    id("com.google.devtools.ksp")
    id("flipper.anvil")
    id("kotlinx-serialization")
}

android.namespace = "dev.namelessnanashi.flipperzero.bridge.connection.sample.android"

android {
    defaultConfig {
        applicationId = "dev.namelessnanashi.flipperzero.bridge.connection"
    }
}

dependencies {
    implementation(projects.components.bridge.connection.sample.shared)
    implementation(projects.components.core.di)
}
