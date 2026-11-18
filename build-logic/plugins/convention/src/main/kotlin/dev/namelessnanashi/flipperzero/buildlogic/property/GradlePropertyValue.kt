package dev.namelessnanashi.flipperzero.buildlogic.property

import dev.namelessnanashi.flipperzero.buildlogic.util.ProjectExt.localProperties
import org.gradle.api.Project

class GradlePropertyValue(
    private val project: Project,
    override val key: String,
) : PropertyValue {
    override fun getValue(): Result<String> {
        return runCatching {
            val gradlePropertyProvider = project.providers.gradleProperty(key)
            if (gradlePropertyProvider.isPresent) {
                gradlePropertyProvider.get()
            } else {
                project.localProperties[key]?.toString() ?: error("Property $key not found")
            }
        }
    }
}
