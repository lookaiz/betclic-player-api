val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val koin_version: String by project
val dynamodb_version: String by project
val localstack_version: String by project

plugins {
    kotlin("jvm") version "1.9.24"
    id("io.ktor.plugin") version "2.3.11"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

group = "com.betclic.player-api"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

ktor {
    fatJar {
        archiveFileName.set("betclic-tournament-api.jar")
    }
}

dependencies {
    // Server
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

    // DynamoDB
    implementation("software.amazon.awssdk:dynamodb:$dynamodb_version")

    // Koin
    implementation(platform("io.insert-koin:koin-bom:$koin_version"))
    implementation("io.insert-koin:koin-core")
    implementation("io.insert-koin:koin-ktor")

    // SLF4J
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Test
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.testcontainers:localstack:$localstack_version")

    // Test Koin
//    testImplementation("io.insert-koin:koin-test:3.5.6")
//    testImplementation("io.insert-koin:koin-test-junit5:3.5.6")
    testImplementation("io.insert-koin:koin-test")
    testImplementation("io.insert-koin:koin-test-junit5")

    // Test - Mockito
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")

    // Test JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")


}

tasks.test {
    useJUnitPlatform()
}

//tasks.register<Exec>("runServer") {
//    val mainClass = "com.betclic.Application"
//    classpath(sourceSets.getByName("main").runtimeClasspath)
//    classpath(files(kotlin.target.compiledClasspath))
//    main = mainClass
//    args("-P", "dev")
//}
