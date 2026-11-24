pipeline {
    agent none

    triggers {
        pollSCM('H/5 * * * *')
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
        timeout(time: 40, unit: 'MINUTES')
    }

    stages {
        stage('Run smoke tests on dev') {
            steps {
                build job: 'stand-deploy-and-test', parameters: [
                    string(name: 'STAND', value: 'dev'),
                    booleanParam(name: 'DEPLOY', value: false),
                    booleanParam(name: 'RESET_DATA', value: false),
                    booleanParam(name: 'BACKUP', value: false),
                    string(name: 'TESTS', value: 'smoke'),
                    string(name: 'BROWSER', value: 'chrome'),
                    string(name: 'TRIGGER', value: 'auto: tests changed')
                ]
            }
        }
    }
}