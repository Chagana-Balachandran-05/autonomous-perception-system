pipeline {
    agent any

    environment {
        MAVEN_OPTS = '-Xmx1024m'
        PROJECT_NAME = 'autonomous-perception-system'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('SCA - Dependency Check') {
            steps {
                sh 'mvn org.owasp:dependency-check-maven:check'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/dependency-check-report.html',
                                     allowEmptyArchive: true
                }
            }
        }

        stage('SAST - Static Analysis') {
            steps {
                sh 'mvn spotbugs:check'
            }
            post {
                always {
                    publishHTML([
                        reportDir: 'target/site',
                        reportFiles: 'spotbugs.html',
                        reportName: 'SpotBugs Report',
                        keepAll: true
                    ])
                }
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn test -Dtest=*Test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                sh 'mvn test -Dtest=*IntegrationTest'
            }
        }

        stage('Runtime Security Validation') {
            steps {
                sh 'mvn test -Dtest=AutonomousPerceptionSecurityTest'
            }
        }

        stage('Code Coverage') {
            steps {
                sh 'mvn jacoco:report'
            }
            post {
                always {
                    jacoco(
                        execPattern: 'target/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java'
                    )
                }
            }
        }

        stage('Security Gate') {
            steps {
                script {
                    if (currentBuild.result == 'FAILURE') {
                        error('Security gate failed - vulnerabilities detected')
                    }
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar'
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline succeeded - artifact is deployment ready'
        }
        failure {
            echo 'Pipeline FAILED - check security gates and test results'
        }
    }
}
