apply(plugin = "pmd")

configure<PmdExtension> {
    toolVersion = "6.55.0"
    //ruleSetFiles = files("path/to/ruleset1.xml", "path/to/ruleset2.xml")
    //has priority over ruleSets because ruleSetFiles is our configuration based on our need
    //where ruleSets is from pmd plugin
    ruleSetFiles = files("${rootDir}/config/pmd/pmdmain.xml")
    ruleSets = emptyList()
}

val pmdConfiguration by configurations.creating

tasks.withType<Pmd> {
    ignoreFailures = false

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.setDestination(file("$buildDir/reports/pmd/pmd.html"))
    }
}

project.afterEvaluate {
    setupAndroidPmd()
}

fun setupAndroidPmd() {
    val mySourceSets = project.property("mySourceSets") as ArrayList<String>
    mySourceSets.forEach { sourceSet ->
        tasks.create("pmd${sourceSet.capitalize()}", Pmd::class) {
            group = "verification"
            description = "Runs pmd task for ${sourceSet.capitalize()} source set"

            source = fileTree("src/${sourceSet}") {
                include("**/*.java", "**/*.kt")
                exclude(
                    "**/R.java",
                    "**BuildConfig.java",
                    "**/gen/**",
                    "**/generated/**",
                    "**/BuildConfig.java",
                    "**/R.java"
                )
            }

            classpath = files()
        }
    }
}

//wrap all pmd tasks
tasks.register("runAllPmdTask") {
    group = "pmd"
    dependsOn(tasks.withType<Task>().matching { task ->
        task.name.startsWith("pmd")
    })
}
