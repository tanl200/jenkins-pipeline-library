#!/usr/bin/groovy
def call(timeOut, timeUnit, approveUser, slackChannel, slackFile, message) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    def output = ''
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def proceedMessage = """${JOB_NAME} - ${BUILD_NUMBER}: ${config?.message} via ${BUILD_URL}"""

    if (config?.slackFile != null ) {
//        output = readFile("upload/${slackFile}").trim()
        sh ("cat upload/${slackFile}")
    }
    
    slackSend channel: "#${slackChannel ?: builds}", message: proceedMessage
//    sh "echo ${proceedMessage}"

    timeout(time: timeOut ?: 5, unit: timeUnit ?: "DAYS" ) {
        input message: "${proceedMessage}", submitter: approveUser    
    }
}