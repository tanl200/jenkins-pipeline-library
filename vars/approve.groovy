#!/usr/bin/groovy

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def proceedMessage = """{env.JOB_NAME} ${env.BUILD_NUMBER}: Would you like to promote version ${config.version} to apply
"""

//    slackSend channel: "#channel-name", message: proceedMessage
    sh "echo ${proceedMessage}"

    input message:'Approve deployment?', submitter: 'admin'
    
//    try {
//        input id: 'Proceed', message: "\n${proceedMessage}"
//    } catch (err) {
//        approveReceivedEvent(id: id, approved: false)
//        throw err
//    }
//    approveReceivedEvent(id: id, approved: true)
}