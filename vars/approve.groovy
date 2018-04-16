#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    def output = ''
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def proceedMessage = """${JOB_NAME} - ${BUILD_NUMBER}: ${config?.message} via ${BUILD_URL}"""

    if (config?.slackFile != null ) {
//        output = readFile("upload/${config.slackFile}").trim()
        sh ("cat upload/${config.slackFile}")
    }
    
    notify {
        slackChannel: 'k8s-build'
        message: proceedMessage
        title: "Request for Approve"
        title_link: "${BUILD_URL}"
    }
//     slackSend channel: "#${config.slackChannel ?: builds}", message: proceedMessage
//    sh "echo ${proceedMessage}"

    timeout(time: config.timeout ?: 5, unit: config.timeUnit ?: "DAYS" ) {
        input message: "${proceedMessage}", submitter: config.approveUser    
    }
}