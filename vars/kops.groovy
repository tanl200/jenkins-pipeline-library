#!/usr/bin/groovy

def call(body) {

    def parameters = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = parameters
    body()

    if (parameters.action=='create') {
    	sh 'echo create'
    }

    if (parameters.action=='replace') {
    	sh 'echo replace'
    }

    if (parameters.action!='create' && parameters.action!='replace') {
    	sh 'echo unknown'
    }
}
