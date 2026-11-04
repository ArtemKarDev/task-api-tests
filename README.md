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

## Настройки

Все настройки тестов описаны в `src/test/resources`: общие значения в `application.yml`, различия стендов в `application-local.yml`, `application-dev.yml`, `application-stage.yml`. Читает их Spring и складывает в объект `TestProperties`, который получают все классы тестов.

### Выбор стенда

Стенд выбирается параметром или переменной окружения (по умолчанию `local`):

```powershell
mvn test "-Denv=dev"            # параметр запуска
$env:TEST_ENV = "stage"         # или переменная окружения
```

Допустимы только `local`, `dev`, `stage`: при любом другом значении (например, опечатке) запуск останавливается с понятным сообщением, а не идёт молча на другой стенд.

### Как переопределить значение

Любую настройку можно задать, не меняя файлы. Источники, от самого важного к самому слабому:

1. параметр запуска `-Dtests.<имя>` (например, `"-Dtests.base-url=http://localhost:9000"`);
2. переменная окружения `TESTS_<ИМЯ>` (например, `$env:TESTS_BASEURL = "http://localhost:9000"`);
3. файл стенда `application-<стенд>.yml`;
4. общий файл `application.yml`.

Имя переменной окружения получается из имени настройки: убрать дефисы, заменить точки на подчёркивания, перевести в заглавные (`tests.kafka.bootstrap-servers` превращается в `TESTS_KAFKA_BOOTSTRAPSERVERS`).

### Список настроек

| Настройка | Переменная окружения | Что это | Значение по умолчанию (local) |
|---|---|---|---|
| `tests.base-url` | `TESTS_BASEURL` | адрес приложения | `http://localhost:8080` |
| `tests.db.url` | `TESTS_DB_URL` | адрес PostgreSQL | `jdbc:postgresql://localhost:5433/taskdb` |
| `tests.db.user` | `TESTS_DB_USER` | пользователь БД | `taskuser` |
| `tests.db.password` | `TESTS_DB_PASSWORD` | пароль БД | для `local` в `application-local.yml`, для `dev` и `stage` только из переменной окружения |
| `tests.browser.name` | `TESTS_BROWSER_NAME` | браузер | `chrome` |
| `tests.browser.headless` | `TESTS_BROWSER_HEADLESS` | без окна браузера | `false` |
| `tests.browser.size` | `TESTS_BROWSER_SIZE` | размер окна | `1920x1080` |
| `tests.browser.timeout-ms` | `TESTS_BROWSER_TIMEOUTMS` | сколько Selenide ждёт элемент, мс | `6000` |
| `tests.browser.remote-url` | `TESTS_BROWSER_REMOTEURL` | адрес удалённого Selenium (пусто: браузер запускается локально) | пусто |
| `tests.kafka.bootstrap-servers` | `TESTS_KAFKA_BOOTSTRAPSERVERS` | адрес брокера Kafka | `localhost:9092` |
| `tests.kafka.topic-in` | `TESTS_KAFKA_TOPICIN` | топик команд | `task_in` |
| `tests.kafka.topic-in-dlq` | `TESTS_KAFKA_TOPICINDLQ` | топик отклонённых сообщений | `task_in_dlq` |
| `tests.kafka.topic-out` | `TESTS_KAFKA_TOPICOUT` | топик событий | `task_out` |

### Секреты

Пароли в репозиторий не кладём. Для `dev` и `stage` пароль БД передаётся только переменной `TESTS_DB_PASSWORD` (на своём компьютере: `$env:TESTS_DB_PASSWORD = "..."`, в Jenkins: из хранилища Credentials). Исключение: пароль стенда `local` в `application-local.yml` учебный и совпадает с тем, что лежит в `compose.yaml` сервиса.

### Как узнать, откуда пришло значение

Если тест ходит не туда, куда ожидалось, запустите «детектор»: он печатает значение настройки и источник (файл со строкой, параметр или переменная окружения):

```powershell
mvn test "-Dtest=SpringSettingsTests" | Select-String "tests\."
```