def call(Closure body) {  
  withCredentials([
    [$class: 'StringBinding', credentialsId: 'acuo_nexusUsername', variable: 'ORG_GRADLE_PROJECT_nexusUsername'],
    [$class: 'StringBinding', credentialsId: 'acuo_nexusPassword', variable: 'ORG_GRADLE_PROJECT_nexusPassword'],
    [$class: 'StringBinding', credentialsId: 'acuo_security_key', variale: 'acuo_security_key_tmp']
  ]) {
    withEnv([
      'ORG_GRADLE_PROJECT_nexusUrl=https://nexus.acuo.com',
      'acuo_security_key=$acuo_security_key_tmp'
    ]) {
      body()
    }
  }
}
