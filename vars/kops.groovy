#!/usr/bin/groovy

def call(body) {
    def parameters = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = parameters
    body()

    def status = 1 
    def output = ''

    def actions = ['create', 'replace', 'export']
    if (parameters.action=='create') {
    	status = sh(returnStatus: true, script: "echo create > create.log")
    	output = readFile('create.log').trim()
    }

    if (parameters.action=='replace') {
    	status = sh(returnStatus: true, script: "echo replace > replace.log")
    	output = readFile('replace.log').trim()
    }

    if (parameters.action!='export') {
    	output = 'unknown'
    }

/*    if (parameters.action!='create' && parameters.action!='replace' && parameters.action!='export') {
    	output = 'unknown'
    }
*/

	if (assert !(parameters.action in actions)) {
		output = 'unknown'
	}
	
    if ( status != 0) {
    	sh "echo ${output}"
    	currentBuild.result = 'FAILED'
    }
}