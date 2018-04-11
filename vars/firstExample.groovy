#!/usr/bin/groovy

def call(Map parameters = [:], body) {

    sh 'echo 123'
    approve {
    	approveUser = parameters.approveUser ?: "admin"
    	timeout = parameters.timeout ?: 5
    	timeUnit = parameters.timeUnit ?: "DAYS"
    	message = parameters.message ?: "Please input message for Approve function"
    }
}
