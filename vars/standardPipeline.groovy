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
            stage("Compile and Test") {
                steps {
                    aws {
                        useNexus {
                            sh './gradlew clean build -Pprofile=docker -x integrationTest'
                        }
                    }
                }
            }
            stage("Build Image") {
                steps {
                    aws {
                        useNexus {
                            sh './gradlew dockerBuildImage -Pprofile=docker -x integrationTest'
                        }
                    }
                }
            }
            stage("Push Image") {
                // when {
                //     branch "release*"
                // }
                steps {
                    aws {
                        useNexus {
                            sh './gradlew dockerPushImage -Pprofile=docker -x integrationTest'
                        }
                    }
                }
            }
            stage("Kubernetes Deploy") {
                steps {
                    aws {
                        useNexus {

                            sh "echo deploying `./gradlew -q devops:printDockerImageTag`"

                            withKubeConfig(caCertificate: '', credentialsId: 'kubectl', serverUrl: 'https://api.staging.acuo-fs.com') {
                                sh "kubectl get nodes"
                                sh "kubectl -n acuo set image deployment/auth acuo-auth=`./gradlew -q devops:printDockerImageTag`"
                                sh "kubectl -n acuo rollout status deployment/auth"
                            }
                        }
                    }
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
