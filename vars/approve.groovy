#!/usr/bin/groovy
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    def output = ''
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def JSONObject attachment = new JSONObject();
    def JSONArray attachments = new JSONArray();

    def proceedMessage = """${JOB_NAME} - ${BUILD_NUMBER}: ${config?.message} via ${BUILD_URL}"""

    if (config?.slackFile != null ) {
        output = readFile("upload/${config.slackFile}").trim()
        attachment.put('data', output)
        attachments.add(attachment)
    } else {
        output = 'empty'
        attachment.put('data', output)
        attachments.add(attachment)
    }
    
//    slackSend channel: "#${config.slackChannel ?: builds}", message: proceedMessage, attachments: attachments.toString()
    slackSend channel: "#${config.slackChannel ?: builds}", attachments: attachments.toString()
//    sh "echo ${proceedMessage}"

    timeout(time: config.timeout ?: 5, unit: config.timeUnit ?: "DAYS" ) {
        input message: "${proceedMessage}", submitter: config.approveUser    
    }
}