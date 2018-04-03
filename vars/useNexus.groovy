def call(Closure body) {  
  withCredentials([
    [$class: 'StringBinding', credentialsId: 'acuo_nexusUsername', variable: 'ORG_GRADLE_PROJECT_nexusUsername'],
    [$class: 'StringBinding', credentialsId: 'acuo_nexusPassword', variable: 'ORG_GRADLE_PROJECT_nexusPassword'],
    [$class: 'StringBinding', credentialsId: 'acuo_secret_key', variable: 'acuo_security_key']
  ]) {
    withEnv([
      'ORG_GRADLE_PROJECT_nexusUrl=https://nexus.acuo.com',
      'acuo_security_key=$acuo_security_key'
    ]) {
      body()
    }
  }
}
