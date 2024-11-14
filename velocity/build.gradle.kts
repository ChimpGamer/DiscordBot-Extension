repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api(project(":shared"))

    compileOnly("com.velocitypowered:velocity-api:3.1.1")

    compileOnly("com.github.ProxioDev.ValioBungee:RedisBungee-Velocity:0.11.4")
}

tasks {
    shadowJar {
        val buildNumber = System.getenv("BUILD_NUMBER")
        if (buildNumber == null) {
            archiveFileName.set("DiscordBot-Velocity-v${project.version}.jar")
        } else {
            archiveFileName.set("DiscordBot-Velocity-v${project.version}-b$buildNumber.jar")
        }
    }
}