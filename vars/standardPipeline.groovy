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
                steps {
                    aws {
                        useNexus {
                            // we push only release branch
                            switch (env.BRANCH_NAME) {
                                case "master":
                                case "develop":
                                    break
                                default:
                                    sh './gradlew dockerPushImage -Pprofile=docker -x integrationTest'
                                    break
                            }
                        }
                    }
                }
            }
            stage("Deploy") {
                steps {
                    sh "echo to be done"
                    /*sh 'ansible-galaxy install -r devops/ansible-deploy/requirements.yml -p devops/ansible-deploy/roles/'
                    aws {
                        ansiblePlaybook credentialsId: 'pradeep-cloud-user', 
                            extras: "-e app_name=margin -e app_branch=$BRANCH_NAME", 
                            inventory: 'devops/ansible-deploy/inventory/palo-dev', 
                            playbook: 'devops/ansible-deploy/playbook.yml', 
                            sudoUser: null
                    }*/
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