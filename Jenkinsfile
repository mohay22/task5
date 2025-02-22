pipeline {
    agent any

    environment {
        JAVA_HOME = tool name: 'JDK17', type: 'jdk'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/mohay22/task5.git' // Replace with your repository
            }
        }

        stage('Build') {
            steps {
                script {
                    bat 'gradlew.bat clean assembleDebug'
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    bat 'gradlew.bat testDebugUnitTest'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([string(credentialsId: 'sqp_89f9ccefdc118ff34353ac15684e8c22f8aa3d2e', variable: 'SONAR_LOGIN')]) {
                        script {
                            def sonarCommand = "gradlew.bat sonarqube -Dsonar.projectKey=task5 -Dsonar.host.url=http://<SONARQUBE_SERVER_IP>:9000 -Dsonar.login=${SONAR_LOGIN}"
                            // Using bat command for Windows
                            bat sonarCommand
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy') {
            steps {
                echo '🚀 Deploy step - You can integrate Firebase App Distribution or another service here.'
            }
        }
    }

    post {
        always {
            // Archive JUnit test results
            junit 'app/build/test-results/testDebugUnitTest/**/TEST-*.xml'

            // Archive APK files
            archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', fingerprint: true
        }
        success {
            echo '✅ Build & Tests Passed Successfully!'
        }
        failure {
            echo '❌ Build or Tests Failed. Check logs for details.'
        }
    }
}
