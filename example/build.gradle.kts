plugins {
    id("java")
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":kova-core"))
}

kotlin {
    jvmToolchain(17)
}

tasks {
    test {
        dependsOn(":kova-core:kotest")
    }
}
