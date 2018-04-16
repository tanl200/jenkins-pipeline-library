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
    	'#ff0000'
    }

    JSONArray attachments = new JSONArray();
	JSONObject attachment = new JSONObject();
	output = readFile('upload/kops_upload')
	attachment.put('text', "${config.message :? Missing TEXT Field}");
	attachment.put('fallback', "${config.message :? Missing TEXT Field}");
	attachment.put('color',color);
	attachment.put('title',"${config.title :? Missing Title}");
	attachment.put('title_link',"${config.title_link :? Missing Title Link}");
	attachments.add(attachment);

    slackSend channel: "#${config.slackChannel :? k8s-build}", attachments: attachments.toString()
}
