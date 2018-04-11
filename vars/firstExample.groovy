#!/usr/bin/groovy

def call(Map parameters = [:], body) {

    sh 'echo 123'
    approve {
    	environment = "staging"
    	approveUser = "admin"
    	timeout = 5
    	timeUnit = "DAYS"
    }
}
