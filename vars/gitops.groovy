#!/usr/bin/groovy

def call(body) {
    def parameters = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = parameters
    body()

    def status = 1 
    def output = ''

    sshagent (credentials: ['f1fe8468-e322-4b55-8599-0a3a6b79acbb']) {
        sh("git config --global user.email ${parameters?.email}")
        sh("git config --global user.name ${parameters?.user}")
        sh("git checkout -b auto-${BUILD_NUMBER} ")
        sh("git add .")
        sh("git commit -m 'auto-commit-${parameters?.commitMessage}'")
        sh("git push origin auto-${BUILD_NUMBER}")
    }
}