apply(plugin = "pmd")
apply(from = "${rootDir}/scripts/common.gradle") //for appModuleSourceSets

configure<PmdExtension> {
    toolVersion = "6.55.0"

    ignoreFailures = true // Set to true to continue the build even if PMD violations are found

    //ruleSetFiles = files("path/to/ruleset1.xml", "path/to/ruleset2.xml")
    //has priority over ruleSets because ruleSetFiles is our configuration based on our need
    //where ruleSets is from pmd plugin
    ruleSetFiles = files("${rootDir}/config/pmd/pmdmain.xml")
    ruleSets = ruleSets + "android.xml"
}

val pmdConfiguration by configurations.creating

tasks.withType<Pmd> {
    reports {
        xml.required = true
        xml.destination = file("${buildDir.absolutePath}/reports/pmd/pmd.xml")
        html.required = true
        html.destination = file("$buildDir/reports/pmd/report.html")
    }
}

(project.extra["appModuleSourceSets"] as? List<String>)?.forEach { sourceSetName: String ->
    tasks.register("pmd${sourceSetName.replaceFirstChar { it.uppercase() }}", Pmd::class) {
        group = 'verification'
        description = "Runs pmd task for ${sourceSetName.capitalize()} source set"


        source(fileTree("src") {
            include("**/*.java", "**/*.kt")
            exclude("**/R.java", "**BuildConfig.java", "**/gen/**", "**/generated/**", "**/BuildConfig.java", "**/R.java")
        })

        classpath = files()
    }
}

tasks.register<Pmd>("taskPmd") {
    group = "Verification"
    ruleSetFiles = files("${rootDir}/config/pmd/pmdmain.xml")
    ignoreFailures = false
    source(fileTree("src") {
        include("**/*.java", "**/*.kt")
        exclude("**/R.java", "**BuildConfig.java", "**/gen/**")
    })
    classpath = files()
}