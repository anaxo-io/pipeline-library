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
                //      branch "ci-cd-pipeline"
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
                            useKubeConfig {
                                sh "./gradlew kubernetesNodes"
                                sh "./gradlew kubernetesDeploy"
                                sh "./gradlew kubernetesDeployStatus"
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
