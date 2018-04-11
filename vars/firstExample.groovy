#!/usr/bin/groovy

def call(Map parameters = [:], body) {

    sh 'echo 123'
    approve {
    	environment = "staging"
    }
}
