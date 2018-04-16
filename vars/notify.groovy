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
    	color = '#008000'
    } else {
    	color = '#ff0000'
    }

    JSONArray attachments = new JSONArray();
	JSONObject attachment = new JSONObject();
	attachment.put('text', "${config?.message}");
	attachment.put('fallback', "${config?.message}");
	attachment.put('color', '#ff0000');
	attachment.put('title',"${config?.title}");
	attachment.put('title_link',"${config?.title_link}");
	attachments.add(attachment);

    slackSend(color: '#ff0000', channel: "#${config.slackChannel}", attachments: attachments.toString())
}
