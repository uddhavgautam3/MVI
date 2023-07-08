pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
        flatDir {
            dirs("libs")
        }
    }
}

rootProject.name = "MVI"
include(":app")
include(":agemodule")
//project(":agemodule").name = "my-agemodule"