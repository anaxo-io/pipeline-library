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
            stage ('Info') {
                steps {
                    sh "java -version"
                    sh "docker version"
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
            stage("Deploy") {
                steps {
                    sh "echo to be done"

                    withKubeConfig(caCertificate: '', credentialsId: 'kubectl', serverUrl: 'https://api.staging.acuo-fs.com') {
                        sh "kubectl get nodes"
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
