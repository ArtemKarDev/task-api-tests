pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    parameters {
        choice(name: 'STAND', choices: ['stage', 'dev', 'local'],
               description: 'Стенд, в сеть которого подключается заглушка. Стенд должен быть уже поднят')
        choice(name: 'ACTION', choices: ['status', 'up', 'restart', 'down'],
               description: 'status: состояние; up: собрать и запустить; restart: перезапуск без пересборки; down: остановить')
        string(name: 'TRIGGER', defaultValue: 'manual', description: 'Источник запуска (заполняют вызывающие задания)')
    }

    environment {
        DOCKER_BUILDKIT = '1'
        COMPOSE = "docker compose -p stub-${params.STAND} -f wiremock/compose.yaml --env-file stub.env"
    }

    stages {
        stage('Prepare') {
            steps {
                script {
                    currentBuild.displayName = "#${env.BUILD_NUMBER} wiremock ${params.STAND}:${params.ACTION}"
                }
                withCredentials([file(credentialsId: "env-${params.STAND}", variable: 'ENV_FILE')]) {
                    // Берём из секретного файла только две нужные строки, остальное не трогаем
                    sh '''
                        PROJECT=$(grep '^COMPOSE_PROJECT_NAME=' "$ENV_FILE" | cut -d= -f2-)
                        PORT=$(grep '^WIREMOCK_PORT=' "$ENV_FILE" | cut -d= -f2-)
                        test -n "$PROJECT" || { echo "В env-$STAND нет COMPOSE_PROJECT_NAME"; exit 1; }
                        test -n "$PORT" || { echo "В env-$STAND нет WIREMOCK_PORT"; exit 1; }
                        printf 'STAND_NETWORK=%s_default\\nWIREMOCK_PORT=%s\\n' "$PROJECT" "$PORT" > stub.env
                        cat stub.env
                    '''
                }
            }
        }

        stage('Run action') {
            steps {
                script {
                    switch (params.ACTION) {
                        case 'status':
                            sh '$COMPOSE ps'
                            break
                        case 'up':
                            sh '''
                                . ./stub.env
                                docker network inspect "$STAND_NETWORK" > /dev/null \
                                  || { echo "Сети $STAND_NETWORK нет: сначала задеплойте стенд $STAND"; exit 1; }
                                $COMPOSE up -d --build --wait --wait-timeout 120
                            '''
                            break
                        case 'restart':
                            sh '$COMPOSE restart'
                            break
                        case 'down':
                            sh '$COMPOSE down'
                            break
                    }
                }
            }
        }

        stage('Smoke check') {
            when { expression { params.ACTION in ['up', 'restart'] } }
            steps {
                // Проверка изнутри сети стенда: так же, как видят заглушку приложение и тесты
                sh '''
                    . ./stub.env
                    docker run --rm --network "$STAND_NETWORK" curlimages/curl:8.10.1 \
                      -sf --retry 10 --retry-connrefused --retry-delay 2 \
                      -X POST http://wiremock:8080/oauth/token
                    echo
                    docker run --rm --network "$STAND_NETWORK" curlimages/curl:8.10.1 \
                      -sf http://wiremock:8080/employees/smoke-check
                    echo
                '''
            }
        }
    }

    post {
        always {
            sh '''
                $COMPOSE ps || true
                $COMPOSE logs --no-color --tail 200 > "wiremock-logs-${STAND}.txt" 2>&1 || true
            '''
            archiveArtifacts artifacts: "wiremock-logs-${params.STAND}.txt", allowEmptyArchive: true
        }
        failure {
            withCredentials([string(credentialsId: 'telegram-bot-token', variable: 'TG_TOKEN'),
                             string(credentialsId: 'telegram-chat-id', variable: 'TG_CHAT')]) {
                sh '''
                    curl -s -S --max-time 20 "https://api.telegram.org/bot${TG_TOKEN}/sendMessage" \
                      --data-urlencode "chat_id=${TG_CHAT}" \
                      --data-urlencode "text=❌ WireMock ${STAND}: ${ACTION} failed
                    Trigger: ${TRIGGER:-manual}
                    Build #${BUILD_NUMBER}
                    ${BUILD_URL}console" \
                      -o /dev/null -w "Telegram: HTTP %{http_code}\\n" || true
                '''
            }
        }
    }
}