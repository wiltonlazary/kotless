package io.kotless.plugin.gradle

import io.kotless.DSLType
import io.kotless.plugin.gradle.dsl.kotless
import io.kotless.plugin.gradle.tasks.*
import io.kotless.plugin.gradle.utils.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.kotlin.dsl.getPlugin

/**
 * Implementation of Kotless plugin
 *
 * It defines tasks to generate and then deploy code written with Kotless.
 *
 * Note: Kotless is using own terraform binary that will be downloaded
 * with `download_terraform` task
 *
 * Also note: Plugin depends on shadowJar plugin and if it was not applied
 * already KotlessPlugin will apply it to project.
 */
class KotlessPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPluginSafely("com.github.johnrengelman.shadow")
            applyPluginSafely("application")

            with(tasks) {
                val shadowJar = getByName("shadowJar")

                val generate = myCreate("generate", KotlessGenerate::class)
                val download = myCreate("download_terraform", TerraformDownload::class)

                val init = myCreate("initialize", TerraformOperation::class) {
                    dependsOn(download, generate, shadowJar)

                    operation = TerraformOperation.Operation.INIT
                }

                myCreate("plan", TerraformOperation::class) {
                    dependsOn(init)

                    operation = TerraformOperation.Operation.PLAN
                }

                myCreate("deploy", TerraformOperation::class) {
                    dependsOn(init)

                    operation = TerraformOperation.Operation.APPLY
                }

                afterEvaluate {
                    if (kotless.extensions.terraform.allowDestroy) {
                        myCreate("destroy", TerraformOperation::class) {
                            dependsOn(init)

                            operation = TerraformOperation.Operation.DESTROY
                        }
                    }

                    run {
                        configurations.create(myLocalConfigurationName)

                        convention.getPlugin<ApplicationPluginConvention>().mainClassName = when (kotless.config.dsl.type) {
                            DSLType.Kotless -> "io.kotless.local.MainKt"
                            DSLType.Ktor -> "io.kotless.local.ktor.MainKt"
                        }

                        myCreate("local", KotlessLocal::class) {
                            dependsOn(tasks.getByName("classes"))
                        }.finalizedBy("run")
                    }
                }
            }
        }
    }
}
