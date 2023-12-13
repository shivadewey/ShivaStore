pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // do not use curly braces here for maven
        maven (
            url  =  uri("https://phonepe.mycloudrepo.io/public/repositories/phonepe-intentsdk-android")
        )
        maven (
                url = uri("https://jitpack.io")
        )
    }
}

rootProject.name = "Shiva Store"
include(":app")
 