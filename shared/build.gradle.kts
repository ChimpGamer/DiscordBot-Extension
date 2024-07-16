dependencies {
    compileOnly("dev.dejvokep:boosted-yaml:1.3.5")

    compileOnly("org.incendo:cloud-core:2.0.0-rc.2")

    compileOnly("com.google.code.gson:gson:2.10.1")

    compileOnly("com.gitlab.ruany", "LiteBansAPI", "0.3.5")

    implementation("net.dv8tion:JDA:5.0.1") {
        exclude("club.minnced", "opus-java")
    }

    implementation("club.minnced:jda-ktx:0.11.0-beta.20")
}