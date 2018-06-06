def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    
    if (env.BRANCH_NAME == "develop") {
        pipeline {

            agent { label 'ubuntu_agent' }
            triggers {
                pollSCM("")
            }
            
            stages {
                stage ('Checkout') {
                    steps {
                        deleteDir()
                        checkout scm
                    }
                }
                stage ('Create Build Metadata') {
                    steps {
                        createBuildMetadata()
                    }
                }
                stage ('Print gradle.properties') {
                    steps {
                        sh 'cat gradle.properties'
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
                    steps {
                        aws {
                            useNexus {
                                sh './gradlew dockerPushImage -Pprofile=docker -x integrationTest'
                            }
                        }
                    }
                }
                /*stage("Kubernetes Deploy 'acuo'") {
                    when {
                        expression {
                            return env.BRANCH_NAME == "develop"
                        }
                    }
                    steps {
                        aws {
                            useNexus {
                                withEnv(['K8_NAMESPACE=acuo']) {
                                    useKubeConfig {
                                        kubeDeploy()
                                    }
                                }
                            }
                        }
                    }
                }*/ 
                stage("Kubernetes Deploy 'qa'") {
                    when {
                        expression {
                            return env.BRANCH_NAME == "develop"
                        }
                    }
                    steps {
                        aws {
                            useNexus {
                                withEnv(['K8_NAMESPACE=qa']) {
                                    useKubeConfig {
                                        kubeDeploy()
                                    }
                                }
                            }
                        }
                    }
                }   
                stage("Kubernetes Deploy 'uat'") {
                    when {
                        expression {
                            return env.BRANCH_NAME == "develop"
                        }
                    }
                    steps {
                        aws {
                            useNexus {
                                withEnv(['K8_NAMESPACE=uat']) {
                                    useKubeConfig {
                                        kubeDeploy()
                                    }
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
}
