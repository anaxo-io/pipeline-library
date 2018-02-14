def call(Closure body) {  
    withKubeConfig(caCertificate: '', credentialsId: 'kubectl', serverUrl: 'https://api.staging.acuo-fs.com') {
        body()
    }
}
