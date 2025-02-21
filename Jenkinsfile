pipeline {
    agent any

    environment {
        SONAR_SCANNER = "${tool 'SonarQube Scanner'}/bin/sonar-scanner" // Ensure correct tool name
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/mohay22/task5.git' // Replace with your repository
            }
        }

        stage('Setup JDK & Gradle') {
            steps {
                script {
                    def javaHome = tool name: 'JDK17', type: 'jdk'
                    env.JAVA_HOME = javaHome
                    if (isUnix()) {
                        env.PATH = "${javaHome}/bin:${env.PATH}"
                    } else {
                        env.PATH = "${javaHome}\\bin;${env.PATH}"
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh './gradlew clean assembleDebug'
                    } else {
                        bat 'gradlew.bat clean assembleDebug'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh './gradlew testDebugUnitTest'
                    } else {
                        bat 'gradlew.bat testDebugUnitTest'
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_LOGIN')]) {
                        script {
                            if (isUnix()) {
                                sh "./gradlew sonarqube -Dsonar.login=$SONAR_LOGIN"
                            } else {
                                bat "gradlew.bat sonarqube -Dsonar.login=%SONAR_LOGIN%"
                            }
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
            junit 'app/build/test-results/test*/TEST-*.xml'
            archiveArtifacts artifacts: 'app/build/outputs/**/*.apk', fingerprint: true
        }
        success {
            echo '✅ Build & Tests Passed Successfully!'
        }
        failure {
            echo '❌ Build or Tests Failed. Check logs for details.'
        }
    }
}
