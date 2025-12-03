plugins {
    id("java")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotest)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":kova-core"))
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.framework.engine)
}

kotlin {
    jvmToolchain(17)
}

tasks {
    test {
        dependsOn("kotest")
    }
}
