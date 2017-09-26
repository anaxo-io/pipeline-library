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
            stage("Deploy") {
                steps {
                    sh 'ansible-galaxy install -r devops/ansible-deploy/requirements.yml -p devops/ansible-deploy/roles/'
                    aws {
                        ansiblePlaybook credentialsId: 'pradeep-cloud-user', 
                            extras: "-e app_name=margin -e app_branch=$BRANCH_NAME", 
                            inventory: 'devops/ansible-deploy/inventory/palo-dev', 
                            playbook: 'devops/ansible-deploy/playbook.yml', 
                            sudoUser: null
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