def call(Closure body) {  
  withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', 
    credentialsId: 'acuo_aws_dev', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
    body()
  }
}