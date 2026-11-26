plugins {
    id("flipper.multiplatform-compose")
    id("flipper.multiplatform-dependencies")
    id("flipper.anvil")
    id("kotlinx-serialization")
}

androidDependencies {
    implementation(projects.components.faphub.source.api)
    implementation(projects.components.core.di)
    implementation(projects.components.core.data)
    implementation(projects.components.core.progress)
    implementation(projects.components.core.ktx)
    implementation(projects.components.core.preference)
    implementation(projects.components.core.ui.theme)
    implementation(projects.components.core.ui.res)
    implementation(projects.components.core.ui.ktx)
    implementation(projects.components.bridge.dao.api)
    implementation(projects.components.bridge.connection.feature.common.api)
    implementation(projects.components.bridge.connection.feature.provider.api)
    implementation(projects.components.bridge.connection.feature.rpcinfo.api)
    implementation(projects.components.bridge.connection.feature.storage.api)
    implementation(projects.components.bridge.synchronization.api)
    implementation(projects.components.faphub.target.api)
    implementation(projects.components.faphub.installation.manifest.api)
    implementation(projects.components.faphub.installation.queue.api)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.immutable.collections)
    implementation(libs.kotlin.coroutines)
    implementation(libs.compose.activity)
    implementation(libs.ktx)
    implementation(libs.okio)
}

androidHostTestDependencies {
    implementation(libs.junit)
    implementation(libs.mockk)
    implementation(libs.kotlin.coroutines.test)
    implementation(libs.roboelectric)
}
