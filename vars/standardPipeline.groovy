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
            stage ('Clone') {
                steps {
                    checkout scm
                }
            }        
            stage("Build") {
                steps {
                    aws {
                        useNexus {
                            sh './gradlew build -Pprofile=docker  -x test -x integrationTest'
                        }
                    }
                }
            }
            stage ('Deploy') {
                steps {
                    sh "echo 'deploying to server ${config.projectName}...'"
                }
            }
        }
        
        post {
            always {
                junit allowEmptyResults: true, testResults: '${config.projectName}/build/test-results/test/*.xml'
            }
        }

    }
}