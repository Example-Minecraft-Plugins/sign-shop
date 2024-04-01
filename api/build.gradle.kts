import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "me.davipccunha.tests"
version = "1.0-SNAPSHOT"

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    archiveFileName.set("signshop-api.jar")

    destinationDirectory.set(file("D:\\Minecraft Dev\\artifacts"))
}