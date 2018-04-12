#!/usr/bin/groovy

def call(body) {
    def parameters = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = parameters
    body()

    def status = 1 
    def output = ''

    sshagent (credentials: ['f1fe8468-e322-4b55-8599-0a3a6b79acbb']) {
        sh("git config --global user.email 'devops-bot@home.com'")
        sh("git config --global user.name 'devops-bot'"
        sh("git checkout -b 'test-pr' ")
        sh("git add .")
        sh("git commit -m 'update'")
        sh('git push origin test-pr')
    }
}