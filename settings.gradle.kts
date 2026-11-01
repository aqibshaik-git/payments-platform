plugins {
    // Lets Gradle download the right JDK (25) automatically if it isn't installed.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "payments-platform"
include("ledger-service")
