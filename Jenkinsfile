pipeline {
  agent any

  environment {
    DOCKERHUB_REPO = "namws0023/algogo-backend"
    IMAGE_TAG      = "${BUILD_NUMBER}"
    APP_HOST       = "ubuntu@172.31.6.5"
    DEPLOY_SCRIPT  = "/opt/algogo/scripts/deploy.sh"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build Image') {
      steps {
        sh """
          docker build -t ${DOCKERHUB_REPO}:${IMAGE_TAG} .
          docker tag ${DOCKERHUB_REPO}:${IMAGE_TAG} ${DOCKERHUB_REPO}:latest
        """
      }
    }

    stage('Docker Login & Push') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'dockerhub-cred',
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        )]) {
          sh """
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker push ${DOCKERHUB_REPO}:${IMAGE_TAG}
            docker push ${DOCKERHUB_REPO}:latest
          """
        }
      }
    }

    stage('Deploy to App EC2') {
      steps {
        sshagent(credentials: ['app-ec2-ssh']) {
          withCredentials([usernamePassword(
            credentialsId: 'dockerhub-cred',
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASS'
          )]) {
            sh """
              ssh -o StrictHostKeyChecking=no ${APP_HOST} '
                export DOCKERHUB_REPO=${DOCKERHUB_REPO}
                export BACKEND_IMAGE=${DOCKERHUB_REPO}:${IMAGE_TAG}
                export DOCKER_USER=${DOCKER_USER}
                export DOCKER_PASS=${DOCKER_PASS}
                bash ${DEPLOY_SCRIPT}
              '
            """
          }
        }
      }
    }
  }

  post {
    always {
      cleanWs()
    }
  }
}