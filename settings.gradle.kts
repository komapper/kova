plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "kova"

include("kova-core")
include("kova-ktor")

include("example-core")
include("example-ktor")
include("example-exposed")
include("example-hibernate-validator")
include("example-konform")
