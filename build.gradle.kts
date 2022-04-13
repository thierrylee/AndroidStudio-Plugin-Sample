plugins {
    id("org.jetbrains.intellij") version "1.5.2"
    kotlin("jvm") version "1.6.20"
    java
}

group = "com.octo.pluginsample"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2021.1.1")
    plugins.set(
        listOf(
            "com.intellij.java",
            "android"
        )
    )
}
tasks {
    runIde {
        ideDir.set(file("/Applications/Android Studio.app/Contents"))
    }

    patchPluginXml {
        changeNotes.set("""
            Add change notes here.<br>
            <em>most HTML tags may be used</em>        """.trimIndent())
    }
}
tasks.getByName<Test>("test") {
    useJUnitPlatform()
}