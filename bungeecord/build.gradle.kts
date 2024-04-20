repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    api(project(":shared"))

    compileOnly("net.md-5:bungeecord-api:1.16-R0.4")

    compileOnly("nl.chimpgamer.networkmanager:common-proxy:2.15.0-SNAPSHOT")

    compileOnly("com.github.ProxioDev.redisbungee:RedisBungee-Bungee:0.11.2")
}

tasks {
    shadowJar {
        val shadedPackage = "nl.chimpgamer.networkmanager.shaded"
        relocate("net.kyori", "$shadedPackage.kyori")
    }
}