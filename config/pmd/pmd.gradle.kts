apply(plugin = "pmd")
apply(from = "${rootDir}/scripts/common.gradle") //for appModuleSourceSets

configure<PmdExtension> {
    toolVersion = "6.55.0"
    //ruleSetFiles = files("path/to/ruleset1.xml", "path/to/ruleset2.xml")
    //has priority over ruleSets because ruleSetFiles is our configuration based on our need
    //where ruleSets is from pmd plugin
    ruleSetFiles = files("${rootDir}/config/pmd/pmdmain.xml")
    ruleSets = ruleSets + "android.xml"
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

(project.extra["appModuleSourceSets"] as? List<String>)?.forEach { sourceSetName: String ->
    tasks.register("pmd${sourceSetName.replaceFirstChar { it.uppercase() }}", Pmd::class) {
        group = "verification"
        description = "Runs pmd task for ${sourceSetName.capitalize()} source set"


        source(fileTree("src") {
            include("**/*.java", "**/*.kt")
            exclude("**/R.java", "**BuildConfig.java", "**/gen/**", "**/generated/**", "**/BuildConfig.java", "**/R.java")
        })

        classpath = files()
    }
}
