dependencies {
    compileOnly("dev.dejvokep:boosted-yaml:1.3.4")

    compileOnly("cloud.commandframework:cloud-core:1.8.4")

    compileOnly("com.google.code.gson:gson:2.10.1")

    compileOnly("com.gitlab.ruany", "LiteBansAPI", "0.3.5")

    implementation("net.dv8tion:JDA:5.0.0-beta.24") {
        exclude("club.minnced", "opus-java")
    }

    implementation("club.minnced:jda-ktx:0.11.0-beta.20")
}