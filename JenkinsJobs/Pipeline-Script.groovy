node {

    try {
        echo "Current Workspace is: ${WORKSPACE}"

        def FLAVOR = env.FLAVOR ?: 'retail'
        def BUILD_TYPE = env.BUILD_TYPE ?: 'debug'
        def APP_NAME = env.APP_NAME ?: ''
        def SET_GROUPS = env.SET_GROUPS ?: ''
        def ADD_GROUPS = env.ADD_GROUPS ?: ''


        CAMELCASE_BUILT_TYPE = BUILD_TYPE.capitalize()
        VARIANT = "${FLAVOR}${CAMELCASE_BUILT_TYPE}"

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
            echo "Using android branch: ${env.ANDROID_BRANCH}"
            checkout([$class                           : 'GitSCM',
                      branches                         : [[name: env.ANDROID_BRANCH]],
                      doGenerateSubmoduleConfigurations: false,
                      extensions                       : [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'MVI']],
                      submoduleCfg                     : [],
                      userRemoteConfigs                : [[url: 'ssh://git@github.com/uddhavpgautam/MVI.git']]
            ])

            dir('MVI') {
                echo 'Loading build information'
                load('JenkinsJobs/Pipeline-Variables.groovy')
            }

        }

        stage('Test Android') {
            dir('MVI') {
                echo "Building testing with coverage: ${env.CODE_COVERAGE_ENABLED}"

                if(env.CODE_COVERAGE_ENABLED == "true") {
                    sh "./gradlew create${VARIANT}CoverageReport"
                } else {
                    sh "./gradlew agemodule:testDebugUnitTest"
                    sh "./gradlew app:test${FLAVOR}DebugUnitTest"
                }

            }
        }

        stage('Lint Report') {
            sh 'if [ ! -d "AndroidLintReports" ]; then mkdir -p "AndroidLintReports"; fi'
            dir('MVI') {
                sh "./gradlew app:lint${VARIANT}"
                sh "cp app/build/reports/lint-results-${VARIANT}.xml ../AndroidLintReports/lint-results.xml"
                publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true,
                reportDir: 'app/build/reports', reportFiles: "lint-results-${VARIANT}.html",
                reportName: 'Android Lint Report', reportTitles: 'Android Lint Report'])
            }
            recordIssues(tools: [androidLintParser(pattern: '**/AndroidLintReports/lint-results.xml')])
        }

        stage('Build Android') {
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