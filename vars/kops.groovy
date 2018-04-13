#!/usr/bin/groovy

def call(body) {
    def parameters = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = parameters
    body()

    def status = 1 
    def output = ''

    def actions = ['init', 'update']
    if (parameters.action=='init') {
    	status = sh(returnStatus: true, script: "echo create > create.log")
    	output = readFile('create.log').trim()
    }

    if (parameters.action=='update') {
    	status = sh(returnStatus: true, script: "echo update > update.log")
    	output = readFile('replace.log').trim()
    }
    
	if (!actions.contains(parameters.action)) {
		output = 'unknown action'
	}

    if ( status != 0) {
    	currentBuild.result = 'FAILED'
    }
}