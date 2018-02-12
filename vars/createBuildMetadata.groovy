def call(Closure body) {  
    sh 'echo "git_commit=$GIT_COMMIT" >> gradle.properties'
    sh 'echo "build_number=$BUILD_NUMBER" >> gradle.properties'
    sh 'echo "git_branch=$GIT_BRANCH" >> gradle.properties'
    sh 'echo "git_branch_no_prefix=${GIT_BRANCH#*/}" >> gradle.properties'
    sh 'cat gradle.properties'
}