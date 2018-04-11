#!/usr/bin/groovy

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def proceedMessage = """${JOB_NAME} - ${BUILD_NUMBER}: ${config.message}
"""

//    slackSend channel: "#channel-name", message: proceedMessage
    sh "echo ${proceedMessage}"

    timeout(time: config.timeout, unit: config.timeUnit) {
        input message: "${proceedMessage}", submitter: config.approveUser    
    }
    
//    try {
//        input id: 'Proceed', message: "\n${proceedMessage}"
//    } catch (err) {
//        approveReceivedEvent(id: id, approved: false)
//        throw err
//    }
//    approveReceivedEvent(id: id, approved: true)
}