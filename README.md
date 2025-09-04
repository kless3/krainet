# Тестовое задание Java/Kotlin Trainee - Krainet

### Микросервисы:
1. user-service (порт 8081)
   - Управление пользователями
   - JWT аутентификация
   - Flyway миграции

2. notification-service (порт 8082)
   - Обработка событий через Kafka
   - Отправка email-уведомлений

3. Kafka (порт 9092)
   - Брокер сообщений
   - Топик: user-events

## Технологический стек
| Компонент       | Технологии                          |
|-----------------|-------------------------------------|
| Язык            | Java 17                             |
| Фреймворки      | Spring Boot 3.x, Spring Security    |
| База данных     | PostgreSQL 13                       |
| Миграции        | Flyway                              |
| Межсервисное взаимодействие | Apache Kafka            |
| Тестирование    | JUnit 5, Mockito                    |
| Логирование     | SLF4J                               |
| Контейнеризация | Docker + Docker Compose             |

## Запуск системы

### 1. Сборка и запуск
git clone https://github.com/kless3/krainet.git
cd krainet

# Сборка и запуск
mvn clean package
docker-compose up --build
### 2. Проверка работы
## Проверить статус сервисов
docker-compose ps
## Просмотр логов Kafka
docker-compose logs -f kafka
## Конфигурация Kafka
Основные настройки в application.properties
## Тестирование
Система покрыта unit тестами:
# Запуск тестов
mvn test
## Примеры запросов
Регистрация пользователя
curl -X POST http://localhost:8081/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Password123!",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User"
  }'
Получение JWT токена
curl -X POST http://localhost:8081/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
## Миграции базы данных
Структура миграций:
src/main/resources/db/migration/
V1__create_tables.sql
V2__insert_test_data.sql
