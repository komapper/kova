plugins {
    id("java")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotest)
    alias(libs.plugins.power.assert)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":kova-core"))
    implementation(libs.ktor.server.request.validation)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.framework.engine)
    testImplementation(libs.kotest.runner.junit5)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xreturn-value-checker=full")
    }
    jvmToolchain(17)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

@Suppress("OPT_IN_USAGE")
powerAssert {
    functions = listOf("io.kotest.matchers.shouldBe")
}
