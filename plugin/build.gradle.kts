import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

group = "me.davipccunha.tests"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("net.md-5:bungeecord-chat:1.8-SNAPSHOT")
    implementation(project(":api"))
    implementation("redis.clients:jedis:5.2.0-beta1")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    archiveFileName.set("sign-shop.jar")

    destinationDirectory.set(file("D:\\Local Minecraft Server\\plugins"))
}

bukkit {
    name = "sign-shop"
    version = "${project.version}"
    main = "me.davipccunha.tests.signshop.SignShopPlugin"
    description = "Plugin that allows players to create shops using signs."
    author = "davipccunha"
    prefix = "Sign Shop" // As shown in console
    apiVersion = "1.8"
    softDepend = listOf("economy")
}