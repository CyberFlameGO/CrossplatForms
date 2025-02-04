
plugins {
    id("net.kyori.indra")
}

sourceSets {
    main {
        multirelease {
            alternateVersions(16)
        }
    }

    create("java16")
}

// Add more configurations here as necessary
val java16Implementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

dependencies {
    testImplementation("org.geysermc.cumulus:cumulus:1.0-SNAPSHOT") // needed for testing button components
    testImplementation("com.google.code.gson:gson:2.8.6") // needed for cumulus

    compileOnly("org.geysermc.floodgate:api:2.1.0-SNAPSHOT")
    api("cloud.commandframework:cloud-core:1.6.2")
    api("cloud.commandframework:cloud-minecraft-extras:1.6.2")
    api("net.kyori:adventure-api:4.10.1")
    api("net.kyori:adventure-text-serializer-legacy:4.10.1")
    api("org.spongepowered:configurate-yaml:4.1.2")
    api("org.bstats:bstats-base:3.0.0")

    // dependencies for java16 sources (optionally used at runtime)
    java16Implementation("org.geysermc:geyser-api:2.0.2-SNAPSHOT")
    java16Implementation("org.geysermc:base-api:2.0.2-SNAPSHOT")
    java16Implementation("org.geysermc:core:2.0.2-SNAPSHOT") {
        isTransitive = false
    }
}

description = "core"
