pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'registry.example.com'
        K8S_NAMESPACE = 'bookverse'
        // 所有微服务模块列表
        SERVICES = 'bookstore-gateway bookstore-user bookstore-product bookstore-order bookstore-promotion bookstore-message bookstore-admin'
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

        // ==================== 后端构建与测试 ====================
        stage('Backend Build & Test') {
            steps {
                sh 'mvn clean package -B'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        // ==================== 前端构建与检查 ====================
        stage('Frontend - User') {
            steps {
                dir('bookstore-frontend') {
                    sh 'npm ci'
                    sh 'npm run lint --if-present'
                    sh 'npm run build'
                }
            }
        }

        stage('Frontend - Admin') {
            steps {
                dir('bookstore-admin-frontend') {
                    sh 'npm ci'
                    sh 'npm run lint --if-present'
                    sh 'npm run build'
                }
            }
        }

        // ==================== Docker 镜像构建与推送 ====================
        stage('Build & Push Docker Images') {
            steps {
                script {
                    // 构建所有后端微服务镜像
                    for (svc in SERVICES.split(' ')) {
                        def image = "${DOCKER_REGISTRY}/bookverse/${svc}:${BUILD_NUMBER}"
                        sh "docker build -f docker/Dockerfile.service --build-arg MODULE=${svc} -t ${image} ."
                        sh "docker push ${image}"
                    }
                    // 构建前端镜像
                    def frontendImage = "${DOCKER_REGISTRY}/bookverse/bookstore-frontend:${BUILD_NUMBER}"
                    sh "docker build -f docker/Dockerfile.frontend -t ${frontendImage} ."
                    sh "docker push ${frontendImage}"

                    def adminFrontendImage = "${DOCKER_REGISTRY}/bookverse/bookstore-admin-frontend:${BUILD_NUMBER}"
                    sh "docker build -f docker/Dockerfile.admin-frontend -t ${adminFrontendImage} ."
                    sh "docker push ${adminFrontendImage}"
                }
            }
        }

        // ==================== K8s 部署 ====================
        stage('Deploy to K8s') {
            steps {
                script {
                    def allDeployments = SERVICES.split(' ') + ['bookstore-frontend', 'bookstore-admin-frontend']
                    for (dep in allDeployments) {
                        def image = "${DOCKER_REGISTRY}/bookverse/${dep}:${BUILD_NUMBER}"
                        sh "kubectl set image deployment/${dep} ${dep}=${image} -n ${K8S_NAMESPACE}"
                    }
                    // 等待所有 Deployment 滚动更新完成
                    for (dep in allDeployments) {
                        sh "kubectl rollout status deployment/${dep} -n ${K8S_NAMESPACE} --timeout=300s"
                    }
                }
            }
        }

        // ==================== 健康检查 ====================
        stage('Health Check') {
            steps {
                script {
                    // 重试机制：最多重试 5 次，每次间隔 10 秒
                    def maxRetries = 5
                    def retryInterval = 10
                    for (int i = 0; i < maxRetries; i++) {
                        try {
                            sh 'curl -sf http://bookverse.example.com/actuator/health'
                            echo 'Health check passed!'
                            return
                        } catch (Exception e) {
                            if (i < maxRetries - 1) {
                                echo "Health check attempt ${i + 1} failed, retrying in ${retryInterval}s..."
                                sleep(retryInterval)
                            } else {
                                error 'Health check failed after all retries!'
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed! Rolling back...'
            script {
                def allDeployments = SERVICES.split(' ') + ['bookstore-frontend', 'bookstore-admin-frontend']
                for (dep in allDeployments) {
                    sh "kubectl rollout undo deployment/${dep} -n ${K8S_NAMESPACE} || true"
                }
            }
        }
    }
}
