project.extensions.extraProperties.apply {
    set("sonar.projectKey", "sonar_jenkins_mvi")
    set("sonar.projectName", "sonar_jenkins_mvi")
    set("sonar.host.url", "http://localhost:9000")
    set("sonar.token", "squ_9e08213394c71294274213703caa1cd3cf160ead")
    set("sonar.core.codeCoveragePlugin", "jacoco")
    set("sonar.coverage.jacoco.xmlReportPaths", "${rootDir}/xml-coverage-report/jacoco.xml")
}

