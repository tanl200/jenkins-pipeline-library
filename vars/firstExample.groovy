#!/usr/bin/groovy

def call(body) {

    def parameters = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = parameters
    body()

    sh ("echo ${parameters.action}")
}
