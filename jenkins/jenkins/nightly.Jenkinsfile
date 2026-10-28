pipeline {
    agent none

    triggers {
        // Каждую ночь около 02:00 по Москве (строка TZ задаёт часовой пояс)
        cron('TZ=Europe/Moscow\nH 2 * * *')
    }

    options {
        disableConcurrentBuilds()
        timeout(time: 90, unit: 'MINUTES')
    }

    stages {
        stage('Deploy to stage and run full regression') {
            steps {
                build job: 'stand-deploy-and-test', parameters: [
                    string(name: 'STAND', value: 'stage'),
                    booleanParam(name: 'DEPLOY', value: true),
                    booleanParam(name: 'RESET_DATA', value: false),
                    booleanParam(name: 'BACKUP', value: false),
                    string(name: 'TESTS', value: 'regression'),
                    string(name: 'BROWSER', value: 'chrome')
                ]
            }
        }
    }
}