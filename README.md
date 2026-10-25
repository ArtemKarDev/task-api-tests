генерация моделей из openapi
плагином openapi-generator-maven-plugin
mvn openapi-generator:generate@task-api-models

запуск allure отчета
mvn allure:report
http://localhost:63342/task-api-tests/target/site/allure-maven-plugin/index.html
mvn allure:serve




mvn test "-Dgroups=ui" "-Dheadless=true"
mvn test "-Dgroups=api"
mvn test "-Dgroups=smoke"
mvn test "-Dgroups=api & smoke"