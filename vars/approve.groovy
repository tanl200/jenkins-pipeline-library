#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]

    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def proceedMessage = """${JOB_NAME} - ${BUILD_NUMBER}: ${config?.message} via ${BUILD_URL}"""
    
    // slackSend channel: "#${config.slackChannel ?: builds}", message: proceedMessage

    notify {
        slackChannel: "#${config.slackChannel ?: builds}"
        message: proceedMessage
        title: "${JOB_NAME} - ${BUILD_NUMBER}: Request for Approve"
        title_link: "${BUILD_NUMBER}"
    }

    timeout(time: config.timeout ?: 5, unit: config.timeUnit ?: "DAYS" ) {
        input message: "${proceedMessage}", submitter: config.approveUser    
    }
}