node {

    try {
        echo "Current Workspace is: ${WORKSPACE}"

        def FLAVOR = env.FLAVOR ?: 'retail'
        def BUILD_TYPE = env.BUILD_TYPE ?: 'debug'
        def APP_NAME = env.APP_NAME ?: ''

        if(APP_NAME == "") {
            echo "Error: APP_NAME not defined!"
            currentBuild.result = 'FAILURE'
        }

        stage('Clean Workspace') {
            if(env.CLEAN_BUILD == "true") {
                echo 'Cleaning existing directories'
                sh 'if [ -d "MVI" ]; then rm -Rf MVI; fi'
            }
        }

        stage('Setup Source') {
            echo "Using android branch: ${env.ANDROID_BRANCH}"
            checkout([$class: 'GitSCM',
                branches: [[name: env.ANDROID_BRANCH]],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'MVI']],
                    submoduleCfg: [],
                    userRemoteConfigs: [[url: 'ssh://git@github.com/uddhavpgautam/MVI.git']]
            ])

            dir('MVI') {
                echo 'Loading build information'
                load('JenkinsJobs/Pipeline-Variables.groovy')
            }

        }

        VARIANT = "Release"
        echo "Building variant: ${env.VARIANT} (${FLAVOR} ${BUILD_TYPE})"

        stage('Build Android') {
            dir('MVI') {
                sh "./gradlew app:assemble${VARIANT}"
            }
        }


    } catch (e) {
        currentBuild.result = "FAILED"
        throw e
    } finally {
        //success or failure, always send notification
    }

}