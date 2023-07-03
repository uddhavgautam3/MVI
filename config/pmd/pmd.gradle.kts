apply(plugin = "pmd")

configure<PmdExtension> {
    toolVersion = "6.55.0"
}

val pmdConfiguration by configurations.creating

tasks.withType<Pmd> {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.register<Pmd>("taskPmd") {
    group = "Verification"
    ruleSetFiles = files("${project.rootDir}/config/pmd/pmdmain.xml")
    ignoreFailures = false
    source(fileTree("src") {
        include("**/*.java", "**/*.kt")
        exclude("**/R.java", "**BuildConfig.java", "**/gen/**")
    })
    classpath = files()
}