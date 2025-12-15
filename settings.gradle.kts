plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "kova"

include("kova-core")
include("kova-factory")
include("kova-ktor")

include("example-core")
include("example-factory")
include("example-ktor")
include("example-exposed")
