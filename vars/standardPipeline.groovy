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

                            withKubeConfig(caCertificate: '', credentialsId: 'kubectl', serverUrl: 'https://api.staging.acuo-fs.com') {
                                sh "kubectl -n ${config.kubernetesNamespace} set image deployment/${config.kubernetesDeployment} ${config.projectName}=`./gradlew -q devops:dockerImageTag`"
                                sh "kubectl -n ${config.kubernetesNamespace} rollout status deployment/${config.kubernetesDeployment}"
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
