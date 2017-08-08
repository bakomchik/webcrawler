pipeline {
  agent any
  stages {
    stage('error') {
      steps {
        input(message: 'Should', id: 'asasas', ok: '1')
        sh 'mvn clean package'
      }
    }
  }
}