pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timeout(time: 40, unit: 'MINUTES')
    }

    parameters {
        choice(name: 'STAND', choices: ['stage', 'dev'], description: 'Стенд, против которого запускаются тесты')
        choice(name: 'TESTS', choices: ['api | db', 'ui', 'smoke', 'regression', 'api', 'db', 'unit'], description: 'Теги JUnit (параметр -Dgroups)')
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Браузер для UI-тестов (запускается в контейнере Selenium)')
    }

    environment {
        MAVEN_IMAGE = 'maven:3.9.16-eclipse-temurin-21'
        CHROME_IMAGE = 'selenium/standalone-chrome:4.51.0-20261010'
        FIREFOX_IMAGE = 'selenium/standalone-firefox:4.51.0-20261010'
    }

    stages {
        stage('Prepare') {
            steps {
                script {
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${params.STAND}: ${params.TESTS}"
                    env.START_TS = sh(returnStdout: true, script: 'date -u +%Y-%m-%dT%H:%M:%SZ').trim()
                    // Браузер нужен, если в выборке есть UI-тесты (ui, smoke, regression)
                    env.NEED_BROWSER = (params.TESTS ==~ /.*(ui|smoke|regression).*/) ? 'true' : 'false'
                }
            }
        }

        stage('Check stand') {
            steps {
                withCredentials([file(credentialsId: "env-${params.STAND}", variable: 'ENV_FILE')]) {
                    sh '''
                        set -eu
                        tr -d '\\r' < "$ENV_FILE" > "/tmp/stand-${BUILD_NUMBER}.env"
                        set -a; . "/tmp/stand-${BUILD_NUMBER}.env"; set +a

                        STATE=$(docker inspect -f '{{.State.Health.Status}}' "${COMPOSE_PROJECT_NAME}-app-1" 2>/dev/null || echo missing)
                        echo "Состояние приложения стенда ${STAND}: $STATE"
                        [ "$STATE" = "healthy" ] || { echo "Стенд не готов: запустите задание stand-deploy (STAND=${STAND}, ACTION=up)"; exit 1; }
                    '''
                }
            }
        }

        stage('Start browser') {
            when { expression { env.NEED_BROWSER == 'true' } }
            steps {
                sh '''
                    set -eu
                    set -a; . "/tmp/stand-${BUILD_NUMBER}.env"; set +a

                    if [ "$BROWSER" = "firefox" ]; then IMAGE="$FIREFOX_IMAGE"; else IMAGE="$CHROME_IMAGE"; fi
                    echo "Запуск браузера из образа $IMAGE"

                    docker run -d \
                      --name "selenium-${BUILD_NUMBER}" \
                      --network "${COMPOSE_PROJECT_NAME}_default" \
                      --network-alias selenium \
                      --shm-size=2g \
                      "$IMAGE"
                '''
            }
        }

        stage('Run tests') {
            steps {
                sh '''
                    set -eu
                    set -a; . "/tmp/stand-${BUILD_NUMBER}.env"; set +a

                    NETWORK="${COMPOSE_PROJECT_NAME}_default"
                    CONTAINER="stand-tests-${BUILD_NUMBER}"
                    ARCHIVE="/tmp/tests-source-${BUILD_NUMBER}.tar.gz"

                    BROWSER_ARGS=""
                    if [ "$NEED_BROWSER" = "true" ]; then
                        BROWSER_ARGS="-e BROWSER=$BROWSER -e HEADLESS=true -e REMOTE_URL=http://selenium:4444/wd/hub"
                    fi

                    tar czf "$ARCHIVE" --exclude=.git --exclude=target --exclude=allure-results --exclude=surefire-reports -C "$WORKSPACE" .

                    docker create \
                      --name "$CONTAINER" \
                      --network "$NETWORK" \
                      -v tests-m2-cache:/root/.m2 \
                      -w /workspace \
                      -e TEST_ENV="$STAND" \
                      -e TEST_GROUPS="$TESTS" \
                      -e BASE_URL="http://task-app:8080" \
                      -e DB_URL="jdbc:postgresql://postgres:5432/${DB_NAME}" \
                      -e DB_USER -e DB_PASSWORD \
                      $BROWSER_ARGS \
                      --entrypoint sh \
                      "$MAVEN_IMAGE" \
                      -c 'tar xzf /tmp/tests-source.tar.gz -C /workspace && bash /workspace/jenkins/run-tests.sh'

                    docker cp "$ARCHIVE" "$CONTAINER:/tmp/tests-source.tar.gz"
                    docker start -a "$CONTAINER"
                '''
            }
        }
    }

    post {
        always {
            sh '''
                CONTAINER="stand-tests-${BUILD_NUMBER}"
                rm -rf allure-results surefire-reports
                docker cp "$CONTAINER:/workspace/target/allure-results" allure-results || true
                docker cp "$CONTAINER:/workspace/target/surefire-reports" surefire-reports || true
                mkdir -p allure-results

                if [ -f "/tmp/stand-${BUILD_NUMBER}.env" ]; then
                    set -a; . "/tmp/stand-${BUILD_NUMBER}.env"; set +a
                    docker logs --since "$START_TS" "${COMPOSE_PROJECT_NAME}-app-1" > stand-app.log 2>&1 || true
                    docker logs --since "$START_TS" "${COMPOSE_PROJECT_NAME}-postgres-1" > stand-postgres.log 2>&1 || true

                    {
                        echo "Stand=${STAND}"
                        echo "Tests=${TESTS}"
                        echo "Base_URL=http://task-app:8080"
                        if [ "$NEED_BROWSER" = "true" ]; then echo "Browser=${BROWSER}"; else echo "Browser=not_used"; fi
                        echo "App_image_id=$(docker inspect -f '{{.Image}}' "${COMPOSE_PROJECT_NAME}-app-1" 2>/dev/null | cut -c8-19)"
                        echo "App_started=$(docker inspect -f '{{.State.StartedAt}}' "${COMPOSE_PROJECT_NAME}-app-1" 2>/dev/null)"
                        echo "Tests_commit=$(git -C "$WORKSPACE" rev-parse --short HEAD 2>/dev/null || echo unknown)"
                        echo "Jenkins_build=${BUILD_URL}"
                    } > allure-results/environment.properties
                fi

                docker rm -f "$CONTAINER" "selenium-${BUILD_NUMBER}" 2>/dev/null || true
                rm -f "/tmp/stand-${BUILD_NUMBER}.env" "/tmp/tests-source-${BUILD_NUMBER}.tar.gz"
            '''
            junit allowEmptyResults: true, testResults: 'surefire-reports/*.xml'
            allure includeProperties: false, jdk: '', results: [[path: 'allure-results']]
            archiveArtifacts artifacts: 'stand-*.log', allowEmptyArchive: true
        }
    }
}