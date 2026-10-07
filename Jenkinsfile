pipeline {
    agent any
    tools {
        jdk 'jdk11'
    }
    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }
    stages {
        stage('Build and test') {
            steps {
                sh './mvnw -B -ntp verify'
            }
        }
        stage('Publish to Artifactory') {
            when { branch 'main' }
            steps {
                sh './mvnw -B -ntp deploy -DskipTests'
            }
        }
    }
    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
    }
}
