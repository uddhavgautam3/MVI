//apply<CheckstylePlugin>() //or apply(plugin = "checkstyle")

apply(plugin = "checkstyle")
//$project.rootDir belongs to ageModule's rootdir for ageModule and app's rootdir for app module

/*
CheckstyleExtension is a class provided by the Checkstyle plugin in Gradle.
It represents the extension configuration for the Checkstyle plugin.

Extensions in Gradle allow you to customize the behavior of plugins by providing additional
configuration options. The Checkstyle plugin extension, CheckstyleExtension, provides configuration
options that you can use to customize the Checkstyle plugin's behavior in your Gradle build.

By accessing the CheckstyleExtension and configuring its properties, you can specify the Checkstyle tool version, enable/disable certain features, provide custom configuration files, and more.
 */
configure<CheckstyleExtension> {
    toolVersion = "10.11.0"

    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")

    configProperties.apply {
        set("ignoreFailures", false)
        set("showViolations", true)
        set("lineLength", 120)
        set("checkstyleSuppressionConfig", file("${rootDir}/config/checkstyle"))
        set("checkstyleSuppressionConfigDir", file("${rootDir}/config/checkstyle"))

    }
}
//creates a new Gradle configuration named checkstyleConfiguration using the configurations.creating function.
//In Gradle, configurations.creating is a function that allows you to create a new configuration. It is used to define a new configuration and assign it to a variable.
val checkstyleConfiguration by configurations.creating

tasks.withType<Checkstyle> {
    minHeapSize.set("1000m")
    maxHeapSize.set("4g")

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.stylesheet = resources.text.fromFile(file("${rootDir}/config/checkstyle/xsl/checkstyle-simple.xsl"))
    }
}

project.afterEvaluate {
    setupAndroidCheckStyle()
}

fun setupAndroidCheckStyle() {
    val mySourceSets = project.property("mySourceSets") as ArrayList<String>
    mySourceSets.forEach { sourceSet ->
        tasks.create("checkstyle${sourceSet.capitalize()}", Pmd::class) {
            group = "verification"
            description = "Runs check task for ${sourceSet.capitalize()} source set"

            source = fileTree("src/${sourceSet}") {
                include("**/*.java", "**/*.kt")
                exclude(
                    "**/R.java",
                    "**BuildConfig.java",
                    "**/gen/**"
                )
            }

            classpath = files()

        }
    }
}


//wrap all checkstyle tasks
tasks.register("runAllCheckstyleTask") {
    group = "checkstyle"
    dependsOn(tasks.withType<Task>().matching { task ->
        task.name.startsWith("checkstyle")
    })
}
