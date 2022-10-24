plugins {
    `java-library`
}

repositories {
    mavenCentral()

    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/BlockyDotJar/JDA-Commons")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/BlockyDotJar/Tixte-Java-Library")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

dependencies {
    api("net.dv8tion:JDA:5.0.0-alpha.22")
    api("dev.blocky.library:jda-commons:1.2.0-pr.5")
    api("dev.blocky.library:tixte4j:1.1.1")
    api("org.slf4j:slf4j-api:2.0.3")
    api("ch.qos.logback:logback-classic:1.4.4")

    compileOnly("com.google.errorprone:error_prone_annotations:2.16")
    compileOnly("org.jetbrains:annotations:23.0.0")
}

group = "dev.blocky.discord"
version = "1.0.2"
description = "A Discord bot for sending voice messages."

java {
    sourceCompatibility = JavaVersion.VERSION_19
}
