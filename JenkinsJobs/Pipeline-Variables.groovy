node {
    env.APP_VERSION = "1.0.0"
    env.RELEASE_NOTES_DATE = "06/11/2023"
    env.CODE_COVERAGE_ENABLED = "true"
    env.APPCENTER_UPLOAD = "true"
    env.CODE_QUALITY_ENABLED = "true"

    //one of these: debug, enterpriseQa, release
    env.BUILD_TYPE = "debug"

    //always retailStage given retail and stage are two product flavors with prod. and env dimensions respectively
    env.BUILD_TYPE = "retailStage"
}