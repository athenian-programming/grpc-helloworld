import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.protobuf.gradle.id
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    idea
    application
    java
    alias(libs.plugins.protobuf)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.versions)
    alias(libs.plugins.shadow)
}

group = "org.athenian"
version = "1.0-SNAPSHOT"
val pkg = "org.athenian.helloworld"
val scriptNames = listOf(
    "java_server_script",
    "java_client_script",
    "kotlin_server_withcr_script",
    "kotlin_client_withcr_script",
    "kotlin_server_withoutcr_script",
    "kotlin_client_withoutcr_script",
)

application {
    mainClass = "$pkg.HelloWorldServer"
}

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(libs.coroutines.core)

    implementation(platform(libs.grpc.bom))
    implementation(libs.bundles.grpc)

    implementation(libs.protobuf.kotlin)
    implementation(libs.grpc.kotlin.stub)
    implementation(libs.protobuf.java.util)

    implementation(libs.kotlin.logging)
    implementation(libs.logback.classic)
    implementation(libs.slf4j.jul.to.slf4j)

    compileOnly(libs.annotation.api)

    testImplementation(libs.grpc.testing)
    testImplementation(libs.junit)
}

kotlin {
    jvmToolchain(17)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protoc.get()}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${libs.versions.gengrpc.get()}:jdk8@jar"
        }
        id("kotlin")
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("kotlin")
                id("grpc")    // Generate Java gRPC classes
                id("grpckt")  // Generate Kotlin gRPC using the custom plugin from library
            }
        }
    }
}

tasks.withType<LintTask> {
    // This will exclude all files under build/generated/
    this.source = this.source.minus(fileTree("build")).asFileTree
}
tasks.withType<FormatTask> {
    this.source = this.source.minus(fileTree("build")).asFileTree
}

kotlinter {
    ignoreFormatFailures = false
    ignoreLintFailures = false
    reporters = arrayOf("checkstyle", "plain")
}

tasks.compileKotlin {
    dependsOn(":generateProto")
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.InternalCoroutinesApi"
        )
    }
}

// Fat JAR tasks (using regular JAR task instead of shadowJar due to compatibility issue)
tasks.register<Jar>("java_server") {
    dependsOn(tasks.classes)
    archiveFileName.set("java-server.jar")
    manifest {
        attributes("Main-Class" to "$pkg.HelloWorldServer")
    }
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Jar>("java_client") {
    dependsOn(tasks.classes)
    archiveFileName.set("java-client.jar")
    manifest {
        attributes("Main-Class" to "$pkg.HelloWorldClient")
    }
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Jar>("kotlin_server_withcr") {
    dependsOn(tasks.classes)
    archiveFileName.set("kotlin-server-withcr.jar")
    manifest {
        attributes("Main-Class" to "$pkg.withCR.HelloWorldServer")
    }
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Jar>("kotlin_client_withcr") {
    dependsOn(tasks.classes)
    archiveFileName.set("kotlin-client-withcr.jar")
    manifest {
        attributes("Main-Class" to "$pkg.withCR.HelloWorldClient")
    }
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Jar>("kotlin_server_withoutcr") {
    dependsOn(tasks.classes)
    archiveFileName.set("kotlin-server-withoutcr.jar")
    manifest {
        attributes("Main-Class" to "$pkg.withoutCR.HelloWorldServer")
    }
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Jar>("kotlin_client_withoutcr") {
    dependsOn(tasks.classes)
    archiveFileName.set("kotlin-client-withoutcr.jar")
    manifest {
        attributes("Main-Class" to "$pkg.withoutCR.HelloWorldClient")
    }
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Script tasks
tasks.startScripts {
    enabled = false
}

tasks.register<CreateStartScripts>("java_server_script") {
    mainClass.set("$pkg.HelloWorldServer")
    applicationName = "java-server"
    outputDir = File(layout.buildDirectory.asFile.get(), "tmp")
    classpath = tasks.startScripts.get().classpath
}

tasks.register<CreateStartScripts>("java_client_script") {
    mainClass.set("$pkg.HelloWorldClient")
    applicationName = "java-client"
    outputDir = File(layout.buildDirectory.asFile.get(), "tmp")
    classpath = tasks.startScripts.get().classpath
}

tasks.register<CreateStartScripts>("kotlin_server_withcr_script") {
    mainClass.set("$pkg.withCR.HelloWorldServer")
    applicationName = "kotlin-server-withcr"
    outputDir = File(layout.buildDirectory.asFile.get(), "tmp")
    classpath = tasks.startScripts.get().classpath
}

tasks.register<CreateStartScripts>("kotlin_client_withcr_script") {
    mainClass.set("$pkg.withCR.HelloWorldClient")
    applicationName = "kotlin-client-withcr"
    outputDir = File(layout.buildDirectory.asFile.get(), "tmp")
    classpath = tasks.startScripts.get().classpath
}

tasks.register<CreateStartScripts>("kotlin_server_withoutcr_script") {
    mainClass.set("$pkg.withoutCR.HelloWorldServer")
    applicationName = "kotlin-server-withoutcr"
    outputDir = File(layout.buildDirectory.asFile.get(), "tmp")
    classpath = tasks.startScripts.get().classpath
}

tasks.register<CreateStartScripts>("kotlin_client_withoutcr_script") {
    mainClass.set("$pkg.withoutCR.HelloWorldClient")
    applicationName = "kotlin-client-withoutcr"
    outputDir = File(layout.buildDirectory.asFile.get(), "tmp")
    classpath = tasks.startScripts.get().classpath
}

distributions {
    main {
        contents {
            into("bin") {
                scriptNames.forEach { scriptName ->
                    from(tasks.named(scriptName))
                }
            }
        }
    }
}

tasks.register("stage") {
    dependsOn("installDist")
}

tasks.named<ShadowJar>("shadowJar") {
    scriptNames.forEach { scriptName ->
        dependsOn(tasks.named(scriptName))
        inputs.files(tasks.named(scriptName))
    }
    // Declare ':compileTestJava' as an input of ':shadowJar'
    dependsOn(tasks.named("compileTestJava"))
    inputs.files(tasks.named("compileTestJava"))
}

// Declare ':compileTestJava' as an input of ':distZip'
tasks.named<Zip>("distZip") {
    dependsOn(tasks.named("compileTestJava"))
    inputs.files(tasks.named("compileTestJava"))
}

tasks.named<Tar>("distTar") {
    dependsOn(tasks.named("compileTestJava"))
    inputs.files(tasks.named("compileTestJava"))
}
