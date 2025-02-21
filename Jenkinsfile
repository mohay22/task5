pipeline {
    agent any

    environment {
        SONAR_SCANNER = "${tool 'sonarQube_server'}/bin/sonar-scanner" // SonarQube Scanner tool
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/mohay22/task5.git' // Replace with your repo
            }
        }

        stage('Setup JDK & Gradle') {
            steps {
                script {
                    def javaHome = tool name: 'JDK17', type: 'jdk'  // Ensure JDK 17 is installed
                    env.JAVA_HOME = javaHome
                    env.PATH = "${javaHome}/bin:${env.PATH}"
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh './gradlew clean assembleDebug'  // Linux/macOS
                    } else {
                        bat 'gradlew.bat clean assembleDebug'  // Windows
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh './gradlew testDebugUnitTest'  // Run JUnit tests (Linux/macOS)
                    } else {
                        bat 'gradlew.bat testDebugUnitTest'  // Windows
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {  // Ensure SonarQube is configured in Jenkins
                    script {
                        if (isUnix()) {
                            sh './gradlew sonarqube -Dsonar.login=sqp_89f9ccefdc118ff34353ac15684e8c22f8aa3d2e'  // Linux/macOS
                        } else {
                            bat 'gradlew.bat sonarqube -Dsonar.login=sqp_89f9ccefdc118ff34353ac15684e8c22f8aa3d2e'  // Windows
                        }
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploy step - You can add Firebase or other distribution steps here'
            }
        }
    }

    post {
        always {
            junit 'app/build/test-results/test*/TEST-*.xml'  // Publish JUnit test results
            archiveArtifacts artifacts: 'app/build/outputs/**/*.apk', fingerprint: true  // Archive APKs
        }
        success {
            echo '✅ Build & Tests Passed Successfully!'
        }
        failure {
            echo '❌ Build or Tests Failed. Check logs for details.'
        }
    }
}
