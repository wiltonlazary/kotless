package io.kotless.plugin.gradle.dsl

import io.kotless.plugin.gradle.utils._ext
import io.kotless.plugin.gradle.utils.ext
import org.gradle.api.Project

var Project.kotless: KotlessDSL
    get() = this.ext("kotless")
    set(value) {
        this._ext["kotless"] = value
    }

/** Configuration of Kotless application */
@KotlessDSLTag
fun Project.kotless(configure: KotlessDSL.() -> Unit) {
    kotless = KotlessDSL(project).apply(configure)
}


