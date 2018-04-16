#!/usr/bin/groovy

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    println config.action2
    println config.text2
    def output = ''

	JSONObject attachment = new JSONObject();
	output = readFile('upload/kops_upload').trim()
	attachment.put('text', output);
	attachment.put('fallback', output);
	attachment.put('color','#ff0000');
	JSONArray attachments = new JSONArray();
	attachments.add(attachment);
	println attachments.toString()




    slackSend channel: "#k8s-build", attachments: attachments.toString()
}
