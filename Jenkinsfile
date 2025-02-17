pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                // Checkout your code
                git 'https://github.com/yourusername/yourproject.git'
            }
        }
        stage('Build') {
            steps {
                // Build the project using Gradle
                sh './gradlew clean assembleDebug'
            }
        }
        stage('Test') {
            steps {
                // Run unit tests
                sh './gradlew testDebugUnitTest'
            }
        }
        stage('Deploy') {
            steps {
                // Deploy the APK or do any further steps
                // For example, deploy to Firebase App Distribution or internal testing server
            }
        }
    }
}
