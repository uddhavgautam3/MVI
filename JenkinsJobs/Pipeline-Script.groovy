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
                //sh "./gradlew taskCheckStyle"clear
                //sh "./gradlew taskPMD"
                sh "./gradlew check" //includes spotbugs, checkstyle, and pmd
                sh "./gradlew test${VARIANT}UnitTest"
            }
        }

        stage('SonarQube Analysis') {
            dir('MVI') {
                sh "./gradlew sonar -Dsonar.projectKey=sonar_jenkins_mvi -Dsonar.projectName='sonar_jenkins_mvi' -Dsonar.host.url=http://localhost:9000 -Dsonar.token=squ_8bcff17df2e0c4d3fb61c6e01279aeb7ff87b0dc"
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

        stage('Dokka Report') {
            dir('MVI') {
                sh "./gradlew clean"
                sh "./gradlew dokkaHtml"
            }
        }

        stage('Test') {
            dir('MVI') {
                echo "Building testing with coverage: ${env.CODE_COVERAGE_ENABLED}"

                if (env.CODE_COVERAGE_ENABLED == "true") {
                    sh "./gradlew create${VARIANT}CoverageReport"
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
        //success or failure, always send notification
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
