plugins {
    id("flipper.multiplatform")
    id("flipper.multiplatform-dependencies")
    id("flipper.anvil")
}

androidDependencies {
    implementation(projects.components.faphub.installation.all.api)

    implementation(projects.components.core.di)
    implementation(projects.components.core.log)
    implementation(projects.components.core.progress)

    implementation(projects.components.faphub.installation.queue.api)
    implementation(projects.components.faphub.installation.manifest.api)
    implementation(projects.components.faphub.target.api)
    implementation(projects.components.faphub.dao.api)
    implementation(projects.components.bridge.dao.api)
    implementation(projects.components.bridge.connection.feature.provider.api)
    implementation(projects.components.bridge.connection.feature.storage.api)
    implementation(projects.components.bridge.connection.feature.common.api)

    implementation(libs.okio)

    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.immutable.collections)
}
