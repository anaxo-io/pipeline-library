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
            stage("PostBuild") {
                steps {
                    node {
                    build job: '/acuo-common/develop', quietPeriod: 30
                    def PB = build job: 'PostBuild', propagate: false
                    result = PB.result
                    if (result.equals("SUCCESS")){
                        echo "success"
                        }
                        else {
                            sh "exit 1" // this fails the stage
                        }
                    }
                }
            }
            stage("trace") {
                steps {
                    node {
                    build job: '/acuo-trace/develop', quietPeriod: 30
                    def TB = build job: 'trace', propagate: false
                    result = TB.result
                    if (result.equals("SUCCESS")) {
                        echo "success"
                    }
                        else {
                            sh "exit 2"
                        }
                    }
                }
            }
            stage("persist") {
                steps {
                    node {
                    build job: '/acuo-persist/develop', quietPeriod: 30
                    def PT = build job: 'persist', quietPeriod: 30
                    result = PT.result
                    if (result.equals("SUCCESS")) {
                        echo "success"
                    }
                        else {
                            sh "exit 3"
                        }
                    }
                }
            }
            stage("all") {
                steps{
                        parallel (
                            a: {
                                build job: '/acuo-auth/develop', quietPeriod: 30
                                },
                            b: {
                                build job: '/acuo-agrement/develop', quietPeriod: 30
                                },
                            c: {
                                build job: '/acuo-margin/develop', quietPeriod: 30
                                },
                            d: {
                                build job: '/acuo-valuation/develop', quietPeriod: 30
                                },
                            e: {
                                build job: '/acuo-collateral/develop',quietPeriod: 30
                            }
                            )
                    }
                }       
        }        
    }
}
