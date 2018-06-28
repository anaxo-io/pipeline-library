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
                stage("Build") {
                    steps {
                        aws {
                            useNexus {
                                sh './rewrite-git-url.sh'
                                sh './gradlew snapshot -x test -x integrationTest'
                            }
                        }
                    }
                }           
            }        
        }
    }
}
