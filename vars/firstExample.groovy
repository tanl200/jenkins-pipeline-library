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

	JSONObject attachment = new JSONObject();
	attachment.put('text','I find your lack of faith disturbing!');
	attachment.put('fallback','Hey, Vader seems to be mad at you.');
	attachment.put('color','#ff0000');
	JSONArray attachments = new JSONArray();
	attachments.add(attachment);
	println attachments.toString()

    slackSend channel: "#k8s-build", message: proceedMessage, attachments: attachments.toString())
}
