package com.codekxlabs.eventsourcing

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created on 7/1/25
 * @author Levi Opunga
 **/

class EventSourcingGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.google.devtools.ksp")

        project.dependencies.add("ksp", "com.codekxlabs:event-sourcing-processor:0.0.1-SNAPSHOT")
        project.dependencies.add("implementation", "com.codekxlabs:event-sourcing-annotations:0.0.1-SNAPSHOT")
        project.afterEvaluate {
            val generateJte = project.tasks.findByName("generateJte")
            val kspKotlin = project.tasks.findByName("kspKotlin")

            if (generateJte != null && kspKotlin != null) {
                kspKotlin.mustRunAfter(generateJte)
                kspKotlin.dependsOn(generateJte)
            }
        }
    }
}