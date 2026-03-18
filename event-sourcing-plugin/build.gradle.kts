plugins {
    kotlin("jvm")
    `maven-publish`
    `java-gradle-plugin`

}

group = "com.codekxlabs"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":event-sourcing-annotations"))
    implementation(project(":event-sourcing-processor"))
}

gradlePlugin {
    plugins {
        create("eventSourcingPlugin") {
            id = "com.codekxlabs.event-sourcing"
            implementationClass = "com.codekxlabs.eventsourcing.EventSourcingGradlePlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}




publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
        }
    }
    repositories {
        mavenLocal()
    }
}