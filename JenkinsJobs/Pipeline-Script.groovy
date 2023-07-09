node {

    try {
        echo "Current Workspace is: ${WORKSPACE}"

        //def FLAVOR = env.FLAVOR ?: 'retail'
        def FLAVOR = env.FLAVOR ?: 'retailStage'
        def BUILD_TYPE = env.BUILD_TYPE ?: 'debug' //default is debug
        def APP_NAME = env.APP_NAME ?: ''
        def SET_GROUPS = env.SET_GROUPS ?: ''
        def ADD_GROUPS = env.ADD_GROUPS ?: ''

        if (APP_NAME == "") {
            echo "Error: APP_NAME not defined!"
            currentBuild.result = 'FAILURE'
        }

        stage('Clean Workspace') {
            if (env.CLEAN_BUILD == "true") {
                echo 'Cleaning existing directories'
                sh 'if [ -d "MVI" ]; then rm -Rf MVI; fi'
            }
        }

        stage('Setup Source') {
            //ANDROID_BRANCH is from Jenkins config.
            echo "Using android branch: ${env.ANDROID_BRANCH}"
            checkout([$class                           : 'GitSCM',
                      branches                         : [[name: env.ANDROID_BRANCH]],
                      doGenerateSubmoduleConfigurations: false,
                      extensions                       : [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'MVI']],
                      submoduleCfg                     : [],
                      userRemoteConfigs                : [[url: 'ssh://git@github.com/uddhavgautam3/MVI.git']]
            ])

            String androidBranch = "${env.ANDROID_BRANCH}"
            //APP_NAME is from Jenkins pipeline config.
            String androidAppName = "${env.APP_NAME}"
            if(androidBranch.contains("release")) {
                if(androidAppName.contains("EQA")) {
                    BUILD_TYPE = "debug"
                } else {
                    BUILD_TYPE = "release"
                }
            }

            CAMELCASE_BUILT_TYPE = BUILD_TYPE.capitalize()
            VARIANT = "${FLAVOR}${CAMELCASE_BUILT_TYPE}"
            echo "Variant name: $VARIANT"

            dir('MVI') {
                echo 'Loading build information'
                load('JenkinsJobs/Pipeline-Variables.groovy')
            }

        }

        stage('Code Quality (Checkstyle, PMD, Spotbugs)') {
            dir('MVI') {
                echo "Code quality check: ${env.CODE_QUALITY_ENABLED}"
                //sh "./gradlew taskPMD"
                sh "./gradlew check" //includes spotbugs
                sh "./gradlew test${VARIANT}UnitTest"

                sh "./gradlew runAllPmdTask"
                sh "./gradlew runAllCheckstyleTask"
            }
        }

        stage('Test, included Jacoco if CODE_COVERAGE is enabled') {
            dir('MVI') {
                echo "Building testing with coverage: ${env.CODE_COVERAGE_ENABLED}"

                if (env.CODE_COVERAGE_ENABLED == "true") {
                    if(BUILD_TYPE == "debug") {
                        //testCoverageEnabled is true only for debug build type
                        //for app module, should execute in below order
                        sh "./gradlew :app:create${VARIANT.capitalize()}CoverageReport"
                        sh "./gradlew :app:test${VARIANT.capitalize()}UnitTest"

                        //for agemodule module, should execute in below order
                        sh ":agemodule:createDebugCoverageReport"
                        sh "./gradlew :agemodule:testDebugUnitTest"
                    }
                } else {
                    //for agemodule only include unit tests as there are no flavors
                    sh "./gradlew agemodule:testDebugUnitTest" //not included on ./gradlew test
                    sh "./gradlew test"
                    //sh "./gradlew app:test${FLAVOR}DebugUnitTest" //included on ./gradlew test
                }

            }
        }

        stage('Build') {
            dir('MVI') {
                sh "./gradlew app:assemble${VARIANT}"
            }
        }

        stage('SonarQube Analysis') {
            dir('MVI') {
                sh "./gradlew sonar -Dsonar.projectKey=sonar_jenkins_mvi -Dsonar.projectName='sonar_jenkins_mvi' -Dsonar.host.url=http://localhost:9000 -Dsonar.token=squ_9e08213394c71294274213703caa1cd3cf160ead"
            }
        }

        stage('Lint Report') {
            sh 'if [ ! -d "AndroidLintReports" ]; then mkdir -p "AndroidLintReports"; fi'
            dir('MVI') {
                sh "./gradlew app:lint${VARIANT}"
                sh "cp app/build/reports/lint-results-${VARIANT}.xml ../AndroidLintReports/lint-results.xml"
                publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true,
                             reportDir   : 'app/build/reports', reportFiles: "lint-results-${VARIANT}.html",
                             reportName  : 'Android Lint Report', reportTitles: 'Android Lint Report'])
            }
            recordIssues(tools: [androidLintParser(pattern: '**/AndroidLintReports/lint-results.xml')])
        }

        stage('AppCenter Upload') {
            if (env.APPCENTER_UPLOAD == "true") {
                dir('MVI') {
                    def PARAMS = getParams(APP_NAME, env.RELEASE_NOTES_DATE, SET_GROUPS, ADD_GROUPS)
                    sh "./gradlew app:appCenterUpload${VARIANT} ${PARAMS}"
                }
            } else {
                echo "Skipping upload to App Center"
            }
        }

    } catch (e) {
        currentBuild.result = "FAILED"
        throw e
    } finally {
        //success or failure, always send notification to (Slack etc.)
    }

}

static def getParams(appName, notesDate, setGroups, addGroups) {
    def params = "-Dappname=\"${appName}\" -DnotesFrom=\"${notesDate}\""
    if (setGroups != '') {
        params += " -Dusegroups=\"${setGroups}\""
    }
    if (addGroups != '') {
        params += " -Daddgroups=\"${addGroups}\""
    }
    params
}
