node {

    try {
        echo "Current Workspace is: ${WORKSPACE}"

        def FLAVOR = env.FLAVOR ?: 'retail'
        def BUILD_TYPE = env.BUILD_TYPE ?: 'debug'
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

        CAMELCASE_BUILT_TYPE = BUILD_TYPE.capitalize()
        VARIANT = "${FLAVOR}${CAMELCASE_BUILT_TYPE}"

        CAMELCASE_BUILT_TYPE = BUILD_TYPE.capitalize()
        println("Building variant: ${FLAVOR}${CAMELCASE_BUILT_TYPE}")

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
