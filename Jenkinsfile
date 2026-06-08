pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'registry.example.com'
        BACKEND_IMAGE = "${DOCKER_REGISTRY}/bookverse/backend:${BUILD_NUMBER}"
        FRONTEND_IMAGE = "${DOCKER_REGISTRY}/bookverse/frontend:${BUILD_NUMBER}"
        K8S_NAMESPACE = 'bookverse'
    }

    tools {
        maven 'Maven-3.9'
        nodejs 'Node-18'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Build') {
            steps {
                dir('online-bookstore') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Backend Test') {
            steps {
                dir('online-bookstore') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('online-bookstore-frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('Frontend Test') {
            steps {
                dir('online-bookstore-frontend') {
                    sh 'npm run lint'
                }
            }
        }

        stage('Build Backend Docker Image') {
            steps {
                dir('online-bookstore') {
                    sh "docker build -t ${BACKEND_IMAGE} ."
                    sh "docker push ${BACKEND_IMAGE}"
                }
            }
        }

        stage('Build Frontend Docker Image') {
            steps {
                dir('online-bookstore-frontend') {
                    sh "docker build -t ${FRONTEND_IMAGE} ."
                    sh "docker push ${FRONTEND_IMAGE}"
                }
            }
        }

        stage('Deploy to K8s') {
            steps {
                sh "kubectl set image deployment/bookverse-backend bookverse-backend=${BACKEND_IMAGE} -n ${K8S_NAMESPACE}"
                sh "kubectl set image deployment/bookverse-frontend bookverse-frontend=${FRONTEND_IMAGE} -n ${K8S_NAMESPACE}"
                sh "kubectl rollout status deployment/bookverse-backend -n ${K8S_NAMESPACE}"
                sh "kubectl rollout status deployment/bookverse-frontend -n ${K8S_NAMESPACE}"
            }
        }

        stage('Health Check') {
            steps {
                sh 'sleep 30'
                sh 'curl -f http://bookverse.example.com/actuator/health || exit 1'
            }
        }
    }

    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
            // kubectl rollout undo
        }
    }
}
