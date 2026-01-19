pipeline {
  agent any

  environment {
    DOCKERHUB_REPO = "namws0023/algogo-backend"
    IMAGE_TAG      = "${BUILD_NUMBER}"

    APP_HOST       = "ubuntu@172.31.6.5"
    GATEWAY_HOST   = "ubuntu@172.31.41.55"

    DEPLOY_SCRIPT  = "/opt/algogo/scripts/deploy.sh"
    COLOR_SCRIPT   = "/opt/algogo-gw/scripts/current_color.sh"
    SWITCH_SCRIPT  = "/opt/algogo-gw/scripts/switch_upstream.sh"
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

    stage('Detect Active Backend') {
      steps {
        sshagent(credentials: ['gateway-ec2-ssh']) {
          script {
            ACTIVE_COLOR = sh(
              script: """
                ssh -o StrictHostKeyChecking=no ${GATEWAY_HOST} \
                ${COLOR_SCRIPT}
              """,
              returnStdout: true
            ).trim()

            if (ACTIVE_COLOR != "blue" && ACTIVE_COLOR != "green") {
              error "Unable to detect active backend color: ${ACTIVE_COLOR}"
            }

            INACTIVE_COLOR = (ACTIVE_COLOR == "blue") ? "green" : "blue"

            echo "Active backend: ${ACTIVE_COLOR}"
            echo "Inactive backend: ${INACTIVE_COLOR}"
          }
        }
      }
    }

    stage('Deploy Inactive Backend') {
      steps {
        sshagent(credentials: ['app-ec2-ssh']) {
          withCredentials([usernamePassword(
            credentialsId: 'dockerhub-cred',
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASS'
          )]) {
            sh """
              ssh -o StrictHostKeyChecking=no ${APP_HOST} '
                export TARGET_COLOR=${INACTIVE_COLOR}
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

    stage('Switch Gateway Upstream') {
      steps {
        sshagent(credentials: ['gateway-ec2-ssh']) {
          sh """
            ssh -o StrictHostKeyChecking=no ${GATEWAY_HOST} '
              export TARGET_COLOR=${INACTIVE_COLOR}
              bash ${SWITCH_SCRIPT}
            '
          """
        }
      }
    }

    stage('Stop Previous Backend') {
      steps {
        sshagent(credentials: ['app-ec2-ssh']) {
          sh """
            ssh -o StrictHostKeyChecking=no ${APP_HOST} '
              docker stop algogo-backend-${ACTIVE_COLOR} || true
            '
          """
        }
      }
    }
  }

  post {
    success {
      echo "Deployment completed successfully."
    }
    failure {
      echo "Deployment failed. Traffic switch was not applied."
    }
    always {
      cleanWs()
    }
  }
}
