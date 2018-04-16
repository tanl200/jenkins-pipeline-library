#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    def output = ''
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def proceedMessage = """${JOB_NAME} - ${BUILD_NUMBER}: ${config?.message}"""
    
    notify {
        slackChannel = "${config.slackChannel}"
        message = "${proceedMessage}"
        title = "Request for Approve"
        title_link = "${BUILD_URL}"
    }
//     slackSend channel: "#${config.slackChannel ?: builds}", message: proceedMessage
//    sh "echo ${proceedMessage}"

    timeout(time: config.timeout ?: 5, unit: config.timeUnit ?: "DAYS" ) {
        input message: "${proceedMessage}", submitter: config.approveUser    
    }
}