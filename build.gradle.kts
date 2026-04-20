plugins {
    kotlin("jvm") version "2.3.20"

    alias(libs.plugins.commons)
    alias(libs.plugins.berry)
    alias(libs.plugins.shadow)
}

group = project.property("group") as String
version = project.property("version") as String

repositories {
    mavenCentral()
    maven("https://maven.lyranie.dev")
}

dependencies {
    implementation(libs.commons)
    implementation(libs.berry.parser)
    implementation(libs.gson)

    implementation(kotlin("reflect"))
}

kotlin {
    jvmToolchain(21)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "dev.lyranie.berry.MainKt"
        }
    }
}
