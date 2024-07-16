plugins {
    id("java")
    id("com.diffplug.spotless") version "6.25.0"
}

group = "diruptio"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://repo.diruptio.de/repository/maven-public/")
}

dependencies {
    compileOnly("diruptio:Spikedog:1.2.6")
    compileOnly("org.jetbrains:annotations:24.1.0")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 17
    }

    jar {
        archiveBaseName = "Dynamite"
    }
}