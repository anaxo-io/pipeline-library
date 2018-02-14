def call() {
    sh "./gradlew kubernetesNodes"
    sh "./gradlew kubernetesGetDeployment"
    sh "./gradlew kubernetesDeploy"
    sh "./gradlew kubernetesDeployStatus"
    sh "./gradlew kubernetesGetDeployment"
}