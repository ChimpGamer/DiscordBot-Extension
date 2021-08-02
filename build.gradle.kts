plugins {
    kotlin("jvm") version "1.5.21"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.github.slimjar") version "1.2.1"
}

repositories {
    mavenLocal()
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots")

    maven("https://m2.dv8tion.net/releases")

    maven("https://jitpack.io")

    maven("https://repo.md-5.net/content/repositories/snapshots/")

    maven("https://repo.codemc.org/repository/maven-public")

    maven("https://repo.maven.apache.org/maven2")
}

dependencies {
    implementation("com.jagrosh:jda-utilities-command:3.0.4")
    compileOnly("net.md-5:bungeecord-api:1.16-R0.2-SNAPSHOT")
    //implementation("nl.chimpgamer.networkmanager:api:2.9.0-SNAPSHOT")
    implementation("nl.chimpgamer.networkmanager:bungeecord:2.9.9-SNAPSHOT") {
        exclude("org.bstats:bstats-bungeecord:1.7")
    }
    slim("net.dv8tion:JDA:4.3.0_277") {
        exclude("club.minnced", "opus-java")
    }
    compileOnly("io.github.slimjar:slimjar:1.2.4")
    compileOnly("com.github.Carleslc:Simple-YAML:1.7.2")
    compileOnly("com.imaginarycode.minecraft:RedisBungee:0.3.8-SNAPSHOT")
    compileOnly(kotlin("stdlib-jdk8"))
}

group = "nl.chimpgamer.networkmanager.extensions"
version = "1.3.7-SNAPSHOT"

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    processResources {
        expand("version" to project.version)
    }

    shadowJar {
        archiveFileName.set("${project.name}-v${project.version}.jar")

        dependencies {
            include(dependency("nl.chimpgamer.networkmanager.extensions:.*"))
            include(dependency("com.jagrosh:.*"))
        }

        val shadedPackage = "nl.chimpgamer.networkmanager.shaded"
        val libPackage = "nl.chimpgamer.networkmanager.lib"
        relocate("io.github.slimjar", "$shadedPackage.slimjar")
        relocate("net.kyori", "$shadedPackage.kyori")

        relocate("kotlin", "$libPackage.kotlin")
        relocate("org.simpleyaml", "$libPackage.simpleyaml")
        relocate("org.eclipse.jetty", "$libPackage.jetty")
        relocate("javax.servlet", "$libPackage.javax.servlet")
        relocate("com.jagrosh.jdautilities", "$shadedPackage.com.jagrosh.jdautilities")
    }

    slimJar {
        shade = false

        relocate("net.dv8tion.jda", "nl.chimpgamer.networkmanager.lib.jda")
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}