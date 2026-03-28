import org.gradle.api.tasks.JavaExec

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.serialization") version "1.9.25"
    application
    id("io.ktor.plugin") version "2.3.12"
}

group = "com.pbshop"
version = "0.1.0"

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ApplicationKt")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.12")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.12")
    implementation("io.ktor:ktor-server-websockets-jvm:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.12")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.12")
    implementation("io.ktor:ktor-server-default-headers-jvm:2.3.12")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.12")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.jetbrains.exposed:exposed-core:0.52.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.52.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.52.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.52.0")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.12")
    testImplementation("io.ktor:ktor-client-websockets-jvm:2.3.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.25")
}

fun registerDbTask(name: String, description: String, command: String) {
    tasks.register<JavaExec>(name) {
        group = "pbshop"
        this.description = description
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass.set("com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbCliKt")
        args(command)
        workingDir = projectDir
        dependsOn(tasks.named("classes"))
    }
}

registerDbTask("dbBootstrap", "Apply PBShop PostgreSQL bootstrap SQL.", "bootstrap")
registerDbTask("dbSeed", "Apply PBShop PostgreSQL sample data SQL.", "seed")
registerDbTask("dbInit", "Apply PBShop PostgreSQL bootstrap and sample data SQL.", "init")
registerDbTask("dbSmoke", "Run PBShop PostgreSQL smoke validation.", "smoke")

tasks.register<JavaExec>("docsExport") {
    group = "pbshop"
    description = "Export PBShop OpenAPI and Swagger assets."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.docs.DocsCliKt")
    args("docs")
    workingDir = projectDir
    dependsOn(tasks.named("classes"))
}

tasks.test {
    useJUnit()
}
