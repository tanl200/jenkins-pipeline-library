#!/usr/bin/groovy

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def color = ''

    if (currentBuild.currentResult=='SUCCESS') {
    	color = 'good'
    } else {
    	color = 'danger'
    }

    JSONArray attachments = new JSONArray();
	JSONObject attachment = new JSONObject();
	attachment.put('text', "${config?.message}");
	attachment.put('fallback', "${config?.message}");
	attachment.put('color', '#ff0000');
	attachment.put('title',"${config?.title}");
	attachment.put('title_link',"${config?.title_link}");
	attachments.add(attachment);

    slackSend(color: 'good', channel: "#${config.slackChannel}", attachments: attachments.toString())
}
