#!/usr/bin/groovy

def call(body) {

    def parameters = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = parameters
    body()

    try {
        body()
    } catch(e) {
        currentBuild.result = "FAILURE";
        throw e;
    } finally {
				
    	parameters.each{ k, v -> println "${k}:${v}" }
		
	}
}
