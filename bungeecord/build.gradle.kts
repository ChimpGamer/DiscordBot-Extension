repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    api(project(":shared"))

    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")

    compileOnly("com.github.ProxioDev.ValioBungee:RedisBungee-Bungee:0.11.4")
}

tasks {
    shadowJar {
        archiveFileName.set("DiscordBot-BungeeCord-v${project.version}.jar")
        val shadedPackage = "nl.chimpgamer.networkmanager.shaded"
        relocate("net.kyori", "$shadedPackage.kyori")
    }
}