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
    testImplementation(libs.kotest.runner.junit5)
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
