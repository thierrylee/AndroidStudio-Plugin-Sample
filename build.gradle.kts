plugins {
    id("org.jetbrains.intellij") version "1.5.2"
    kotlin("jvm") version "1.6.20"
    java
}

group = "com.octo.pluginsample"
version = "1.0"

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
        pluginDescription.set("""
            A sample developped for testing plugin capabilities
        """.trimIndent())

        changeNotes.set("""
               Initial release
        """.trimIndent())
    }

    signPlugin {
        // Define variables in ~/.gradle/gradle.properties :
        // - androidStudioPluginSigninCertificate as a file path to signing certificate
        // - androidStudioPluginSigninPrivateKey as a file path to private key
        // - androidStudioPluginSigninPrivateKey as a String
        certificateChain.set(file(project.properties["androidStudioPluginSigninCertificate"].toString()).readText())
        privateKey.set(file(project.properties["androidStudioPluginSigninPrivateKey"].toString()).readText())
        password.set(project.properties["androidStudioPluginSigninPassword"].toString())
    }

    publishPlugin {
        // Define variable androidStudioPluginPublishToken as Strinig in ~/.gradle/gradle.properties :
        token.set(project.properties["androidStudioPluginPublishToken"].toString())
    }
}
tasks.getByName<Test>("test") {
    useJUnitPlatform()
}