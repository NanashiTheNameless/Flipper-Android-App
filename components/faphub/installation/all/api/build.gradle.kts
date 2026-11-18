plugins {
    id("flipper.multiplatform")
    id("flipper.multiplatform-dependencies")
}

androidDependencies {
    api(projects.components.faphub.installation.manifest.api)
}
