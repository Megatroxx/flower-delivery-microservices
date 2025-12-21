# Используем официальный образ OpenJDK для сборки
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Копируем файлы конфигурации Gradle
COPY gradle/ ./gradle/
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradlew ./

# Устанавливаем права на выполнение для gradlew
RUN chmod +x gradlew

# Копируем исходный код
COPY src/ ./src/

# Собираем приложение
# Используем задачу fatJar из build.gradle.kts, которая создает JAR с суффиксом -all.jar
RUN ./gradlew fatJar --no-daemon -x test || ./gradlew buildFatJar --no-daemon -x test || ./gradlew build --no-daemon -x test

# Создаем финальный образ
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем собранный JAR файл
# Сначала пробуем найти fat JAR (с суффиксом -all), иначе берем любой JAR из build/libs
COPY --from=build /app/build/libs/ ./libs/
RUN if ls ./libs/*-all.jar 1> /dev/null 2>&1; then \
        cp ./libs/*-all.jar ./app.jar; \
    else \
        cp ./libs/cart-service-*.jar ./app.jar; \
    fi && \
    rm -rf ./libs

# Открываем порт
EXPOSE 8081

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]

