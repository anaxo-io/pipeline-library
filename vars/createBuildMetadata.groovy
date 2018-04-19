def call() {  
    sh 'echo "git_commit=$GIT_COMMIT" >> gradle.properties'
    sh 'echo "build_number=$BUILD_NUMBER" >> gradle.properties'
    sh 'echo "git_branch=$GIT_BRANCH" >> gradle.properties'
    sh 'echo "git_branch_no_prefix=${GIT_BRANCH#*/}" >> gradle.properties'
    sh 'echo "nexusUrl=https://nexus.acuo.com" >> gradle.properties'
    sh 'echo "nexusUsername=deployer" >> gradle.properties'
    sh 'echo "nexusPassword=VBF4ROzCO1P7cUVnE05t" >> gradle.properties'
}
