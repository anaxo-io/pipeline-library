def call(Closure body) {  
  withCredentials([
    [$class: 'StringBinding', credentialsId: 'acuo_nexusUsername', variable: 'ORG_GRADLE_PROJECT_nexusUsername'],
    [$class: 'StringBinding', credentialsId: 'acuo_nexusPassword', variable: 'ORG_GRADLE_PROJECT_nexusPassword']
  ]) {
    withEnv([
      'ORG_GRADLE_PROJECT_nexusUrl=https://nexus.acuo.com'
    ]) {
      body()
    }
  }
}