#!/usr/bin/groovy
def checkDiff() {
    def diff = 0

    diff = sh(returnStdout: true, script: "git --no-pager diff | wc -l ").trim()
    sh("echo diff is : ${diff} ")

    if ( diff != "0" ) {
        return true
    } else {
        return false
    }
}

def call(body) {
    def parameters = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = parameters
    body()

    println "test git parameters"
    println parameters.type
    println parameters.type2

    println "${parameters.type}"
    println "${parameters.type2}"

    println "check branch info"
    println "${parameters.branch}"
    

    if (parameters.type=='push') {
        if (checkDiff()) {
            sshagent (credentials: ['f1fe8468-e322-4b55-8599-0a3a6b79acbb']) {
                sh("git config --global user.email ${parameters?.email}")
                sh("git config --global user.name ${parameters?.user}")
                sh("git checkout -b auto-${BUILD_NUMBER} ")
                sh("git add .")
                sh("git commit -m 'auto-commit-${parameters?.commitMessage}'")
                sh("git push origin auto-${BUILD_NUMBER}")                
            }                    
        }
    }

    if (parameters.type=='pull') {
        sshagent (credentials: ['f1fe8468-e322-4b55-8599-0a3a6b79acbb']) {
            sh("git checkout ${parameters.branch} ")
            sh("git pull")
        }
    }

}