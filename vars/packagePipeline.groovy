def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    
    pipeline {

        agent { label 'ubuntu_agent' }
        triggers {
            pollSCM("")
        }
        
        stages {
            stage ('Checkout') {
                steps {
                    checkout scm
                }
            }        
            stage("Build") {
                steps {
                    aws {
                        useNexus {
                            sh './gradlew snapshot -x test -x integrationTest'
                        }
                    }
                }
            }           
        }        
    }
}
