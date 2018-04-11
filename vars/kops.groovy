#!/usr/bin/groovy

def call(body) {

    def parameters = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = parameters
    body()

    if (parameters.action=='create') {
    	def status = sh(returnStatus: true, script: "echo 123 > create.log")
    	return status
    }

    if (parameters.action=='replace') {
    	def status = sh(returnStatus: true, script: "echo 123 > replace.log")
    	return status
    }

    if (parameters.action!='create' && parameters.action!='replace') {
    	def status = sh(returnStatus: true, script: "echo unknown > unknown.log")
    	return 1
    }
}
