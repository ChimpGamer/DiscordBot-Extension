import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    mavenLocal()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }

    maven { url = uri("https://jcenter.bintray.com") }

    maven { url = uri("https://jitpack.io") }

    maven { url = uri("http://repo.md-5.net/content/repositories/snapshots/") }

    maven { url = uri("https://repo.maven.apache.org/maven2") }
}

dependencies {
    compileOnly("net.kyori:text-api:3.0.4")
    implementation("com.jagrosh:jda-utilities-command:3.0.3")
    compileOnly("nl.chimpgamer.networkmanager:api:2.8.7")
    compileOnly("nl.chimpgamer.networkmanager:bungeecord:2.8.7")
    compileOnly("net.md-5:bungeecord-api:1.15-SNAPSHOT")
    compileOnly("net.dv8tion:JDA:4.1.1_101")
    compileOnly("com.github.Carleslc:Simple-YAML:1.4.1")
    compileOnly("com.imaginarycode.minecraft:RedisBungee:0.3.8-SNAPSHOT")
    compileOnly(kotlin("stdlib-jdk8"))
}

group = "nl.chimpgamer.networkmanager.extensions"
version = "1.3.2"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.processResources {
    val tokens = mapOf("version" to project.version)
    from(sourceSets["main"].resources.srcDirs) {
        filter<ReplaceTokens>("tokens" to tokens)
    }
}

tasks.shadowJar {
    archiveFileName.set("${project.name}-v${project.version}.jar")

    dependencies {
        include(dependency("nl.chimpgamer.networkmanager.extensions:.*"))
        include(dependency("com.jagrosh:.*"))
    }

    relocate("kotlin", "nl.chimpgamer.networkmanager.lib.kotlin")
    relocate("org.simpleyaml", "nl.chimpgamer.networkmanager.lib.simpleyaml")
    relocate("org.eclipse.jetty", "nl.chimpgamer.networkmanager.lib.jetty")
    relocate("javax.servlet", "nl.chimpgamer.networkmanager.lib.javax.servlet")
    relocate("com.google.gson", "nl.chimpgamer.networkmanager.lib.gson")
    relocate("com.jagrosh.jdautilities", "nl.chimpgamer.networkmanager.shaded.com.jagrosh.jdautilities")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    enabled = false
}
