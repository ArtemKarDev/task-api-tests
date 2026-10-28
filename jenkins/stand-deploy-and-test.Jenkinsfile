pipeline {
    agent any

    options {
        timeout(time: 60, unit: 'MINUTES')
    }

    parameters {
        choice(name: 'STAND', choices: ['stage', 'dev'], description: 'Стенд')
        booleanParam(name: 'DEPLOY', defaultValue: true,
                     description: 'Сначала пересобрать и перезапустить стенд (stand-deploy, ACTION=up). Выключите, чтобы прогнать тесты на уже работающем стенде')
        booleanParam(name: 'RESET_DATA', defaultValue: false,
                     description: 'При деплое удалить ВСЕ данные стенда. Только dev, для stage запрещено')
        booleanParam(name: 'BACKUP', defaultValue: false,
                     description: 'Только с RESET_DATA: сохранить дамп БД перед удалением')
        choice(name: 'TESTS', choices: ['api | db', 'ui', 'smoke', 'regression', 'api', 'db', 'unit'],
               description: 'Теги JUnit (параметр -Dgroups)')
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Браузер для UI-тестов')
    }

    stages {
        stage('Check parameters') {
            steps {
                script {
                    if (params.RESET_DATA && params.STAND == 'stage') {
                        error('RESET_DATA для stage запрещён: удаление данных разрешено только для dev')
                    }
                    if (params.RESET_DATA && !params.DEPLOY) {
                        error('RESET_DATA работает только вместе с DEPLOY')
                    }
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${params.STAND}: ${params.TESTS}" + (params.DEPLOY ? ' +deploy' : '')
                }
            }
        }

        stage('Deploy stand') {
            when { expression { params.DEPLOY } }
            steps {
                build job: 'stand-deploy', parameters: [
                    string(name: 'STAND', value: params.STAND),
                    string(name: 'ACTION', value: 'up'),
                    booleanParam(name: 'RESET_DATA', value: params.RESET_DATA),
                    booleanParam(name: 'BACKUP', value: params.BACKUP)
                ]
            }
        }

        stage('Run tests') {
            steps {
                build job: 'stand-tests', parameters: [
                    string(name: 'STAND', value: params.STAND),
                    string(name: 'TESTS', value: params.TESTS),
                    string(name: 'BROWSER', value: params.BROWSER)
                ]
            }
        }
    }
}