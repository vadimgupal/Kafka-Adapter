# Kafka Adapter Project

## Обзор приложения

Проект состоит из двух модулей:

- **httpServer** – Spring MVC приложение, предоставляющее REST API для работы с библиотекой (книги и авторы). Реализованы методы создания, обновления и получения данных о книгах и авторах.  
- **parser** – сервис, который слушает сообщения из Kafka, парсит JSON, содержащий HTTP-запросы, и выполняет их через HTTP-клиент (RestTemplate/Feign).  

Сервисы связаны между собой через Kafka: сообщения с описанием HTTP-запросов приходят в топик, `parser` обрабатывает их и вызывает соответствующие методы на `httpServer`.

---

## Функциональность

- Прием JSON-сообщений из Kafka с описанием HTTP-запросов.  
- Парсинг сообщений с формированием корректного HTTP-запроса (метод, URL, body, заголовки, параметры).  
- Выполнение HTTP-запросов к REST API `httpServer` через встроенный HTTP клиент.  
- CRUD операции для сущностей **Book** и **Author** на Spring MVC сервере.  
- Полное тестирование сервисов и интеграций: Unit тесты с JUnit и Mockito, интеграционные тесты с Embedded Kafka и WireMock.  
- Контейнеризация через Docker и запуск всех сервисов через `docker-compose` (Kafka, Zookeeper, PostgreSQL, Liquibase, httpServer, parser).  
- Автоматическая сборка и тестирование с CI/CD через GitHub Actions.

---

## Используемые технологии

- **Язык и платформа:** Java 21, Spring Boot 3, Spring MVC, Spring Data JPA, Spring Kafka  
- **База данных:** PostgreSQL с миграциями через Liquibase  
- **Сообщения:** Apache Kafka, Zookeeper  
- **Тестирование:** JUnit 5, Mockito, TestContainers, WireMock, Embedded Kafka  
- **Сборка и управление зависимостями:** Maven  
- **Контейнеризация:** Docker, Docker Compose  
- **CI/CD:** GitHub Actions для сборки проекта, запуска тестов и сборки Docker-образов
