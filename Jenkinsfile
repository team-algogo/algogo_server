pipeline {
  agent any

  environment {
    DOCKERHUB_REPO = "namws0023/algogo-backend"
    IMAGE_TAG = "${BUILD_NUMBER}"
    DEPLOY_DIR = "/opt/algogo"
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

    stage('Deploy (Compose Up)') {
      steps {
        sh """
          cd ${DEPLOY_DIR}
          BACKEND_IMAGE=${DOCKERHUB_REPO}:${IMAGE_TAG} docker compose -f docker-compose.prod.yml pull
          BACKEND_IMAGE=${DOCKERHUB_REPO}:${IMAGE_TAG} docker compose -f docker-compose.prod.yml up -d
        """
      }
    }
  }

  post {
    always {
      cleanWs()
    }
  }
}
