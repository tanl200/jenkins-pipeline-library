#!/usr/bin/groovy

def call(body) {

    def parameters = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = parameters
    body()

    sh 'echo 123'
    approve {
    	approveUser = parameters.approveUser ?: "admin"
    	timeout = parameters.timeout ?: 5
    	timeUnit = parameters.timeUnit ?: "DAYS"
    	message = parameters.message ?: "Please input message for Approve function"
    }
}
