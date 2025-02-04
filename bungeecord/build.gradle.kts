import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.18-R0.1-SNAPSHOT")
    api("cloud.commandframework:cloud-bungee:1.6.2")
    api("net.kyori:adventure-platform-bungeecord:4.1.0")
    implementation("org.bstats:bstats-bungeecord:3.0.0")
    api(project(":core"))
}

tasks.withType<ShadowJar> {
    dependencies {
        shadow {
            relocate("cloud.commandframework", "dev.projectg.crossplatforms.shaded.cloud")
            relocate("net.kyori", "dev.projectg.crossplatforms.shaded.kyori")
            relocate("org.spongepowered.configurate", "dev.projectg.crossplatforms.shaded.configurate")
            // Used by cloud and configurate
            relocate("io.leangen.geantyref", "dev.projectg.crossplatforms.shaded.typetoken")
            relocate("org.bstats", "dev.projectg.crossplatforms.shaded.bstats")
        }
        exclude {
                e -> e.name.startsWith("com.mojang") // all available on bungee
                || e.name.startsWith("org.yaml")
                || e.name.startsWith("com.google")
        }
    }

    archiveFileName.set("CrossplatForms-BungeeCord.jar")
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}

description = "bungeecord"
