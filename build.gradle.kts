plugins {
    kotlin("jvm")
    id("fabric-loom")
    `maven-publish`
    java
}

group = property("maven_group")!!
version = property("mod_version")!!

repositories {
    maven {
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    /*
    maven {
        url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    }
    maven { url = uri("https://maven.impactdev.net/repository/development/") }
     */
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
    maven { url= uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    //modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    //modImplementation("curse.maven:cobblemon-687131:${property("cobblemon_curse_file_id")}")
    modImplementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    //modImplementation("local.com.bedrockk:molang:1.1.1")
    //modImplementation("com.eliotlash.molang:molang:18")
    //modImplementation("com.cobblemon:mod:1.2.0+1.19.2")
    modImplementation(include("eu.pb4:placeholder-api:2.0.0-pre.4+1.19.3")!!)
    modImplementation("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")
    include("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")
}

tasks {

    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
            }
        }

        // select the repositories you want to publish to
        repositories {
            // uncomment to publish to the local maven
            // mavenLocal()
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}



// configure the maven publication