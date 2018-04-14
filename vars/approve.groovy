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

    JSONObject attachment = new JSONObject();
    JSONArray attachments = new JSONArray();

    def proceedMessage = "" // ""${JOB_NAME} - ${BUILD_NUMBER}: ${config?.message} via ${BUILD_URL}"""

    if (config?.slackFile != null ) {
        output = readFile("upload/${config.slackFile}").trim()
        proceedMessage = """${JOB_NAME} - ${BUILD_NUMBER}: ${config?.message} via ${BUILD_URL}
    
            ${output} 
        """
    }
    
    slackSend channel: "#${config.slackChannel ?: builds}", message: proceedMessage, attachments: attachments.toString()
//    sh "echo ${proceedMessage}"

    timeout(time: config.timeout ?: 5, unit: config.timeUnit ?: "DAYS" ) {
        input message: "${proceedMessage}", submitter: config.approveUser    
    }
}