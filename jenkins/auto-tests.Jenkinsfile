pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
    }

    options {
        disableConcurrentBuilds()
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
                    string(name: 'BROWSER', value: 'chrome')
                ]
            }
        }
    }
}