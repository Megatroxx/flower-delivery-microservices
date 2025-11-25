plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "com.example.ApplicationKt"
}

dependencies {
    implementation("com.rabbitmq:amqp-client:5.21.0")

    implementation("org.xerial:sqlite-jdbc:3.45.1.0")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.51.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.51.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.51.1")

    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.h2)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.server.cio)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation("io.mockk:mockk:1.13.12")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}

tasks.test {
    useJUnitPlatform()
}
