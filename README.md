# task-api-tests

Автотесты для специально разработанного учебного сервиса [task-api-service](https://github.com/ArtemKarDev/task-api-service): REST API, веб-интерфейс, база данных и интеграция с Kafka. Проект учебный: на нём отрабатываются приёмы построения тестового фреймворка, запуск на нескольких стендах и работа в CI.

## Что тестируем

Сервис управления задачами (`Task`: название, описание, статус, приоритет, исполнитель):

| Часть сервиса | Что проверяем |
|---|---|
| REST API `/api/tasks` | создание, чтение, полная и частичная правка, удаление, формат ошибок, валидация |
| Веб-страницы (Thymeleaf) | поиск, создание и удаление задач, навигация, валидация формы |
| PostgreSQL | данные после операций, граничные значения, очистка |
| Kafka | события `task_out`, команды `task_in`, отклонённые сообщения `task_in_dlq` |

## Стек

Java 21, Maven, JUnit 5, Spring (контекст без веб-сервера: настройки и внедрение зависимостей), REST Assured, Selenide, `kafka-clients`, Awaitility, AssertJ, Allure, OpenAPI Generator (модели API генерируются из `src/test/resources/openapi`).

## Виды тестов и теги

Запуск по тегам: параметр `-Dgroups=...`.

| Тег | Что это | Нужен ли стенд |
|---|---|---|
| `unit` | проверки конфигурации и учебные примеры Spring | нет |
| `api` | REST-тесты | да |
| `db` | проверки базы данных | да |
| `kafka` | события и команды Kafka | да |
| `ui` | тесты браузера (Selenide, Page Object) | да, и Chrome |
| `smoke` | быстрый набор «всё ли живо» (включает API, Kafka и UI) | да |
| `regression` | полный набор | да |
| `negative` | негативные сценарии (дополнительная метка) | да |

Теги можно комбинировать: `"-Dgroups=api | db"` (или), `"-Dgroups=api & smoke"` (и).

## Стенды

У каждого стенда свои приложение, база данных и брокер Kafka.

| Стенд | Приложение | PostgreSQL | Kafka | Где работает приложение |
|---|---|---|---|---|
| `local` | 8080 | 5433 | 9092 | запускается из IDE, БД и Kafka в Docker |
| `dev` | 8180 | 5432 | 9192 | в Docker |
| `stage` | 8280 | 5434 | 9292 | в Docker |

Стенды поднимаются из репозитория сервиса (`docker compose --env-file env/<стенд>.env up -d`), подробности в его README.

## Быстрый старт

Нужны: JDK 21, Maven 3.9+, Docker Desktop, Chrome (для UI-тестов).

1. Поднять стенд (например, `local`: из репозитория сервиса `docker compose --env-file env/local.env up -d postgres kafka`, затем запустить приложение из IDE).
2. Запустить тесты (PowerShell, параметры с `-D` берите в кавычки):

```powershell
# без стенда: проверки конфигурации
mvn test "-Dgroups=unit"

# API и БД на стенде local
mvn test "-Dgroups=api | db"

# быстрый набор на dev; пароль БД стенда передаётся переменной окружения
$env:TESTS_DB_PASSWORD = "<пароль>"
mvn test "-Dgroups=smoke" "-Denv=dev"
```

3. Открыть отчёт Allure:

```powershell
mvn allure:serve
```

Один тестовый класс: `mvn test "-Dtest=CreateTaskTests"`. Тесты без окна браузера: добавить `"-Dtests.browser.headless=true"`.