plugins {
    id("java")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":kova-core"))
    implementation(project(":kova-ktor"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host.jvm)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.kotlin.test)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xreturn-value-checker=full", "-Xcontext-parameters")
    }
    jvmToolchain(17)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
