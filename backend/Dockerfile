# ---------- Estágio 1: compila o jar ----------
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /app

# Copia só o pom primeiro: se o código mudar mas as dependências não, o Docker reaproveita esta camada.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -ntp dependency:go-offline

COPY src src
RUN ./mvnw -B -ntp package -DskipTests

# ---------- Estágio 2: imagem final, só com o necessário para rodar ----------
FROM eclipse-temurin:21-jre-noble

# Tesseract + modelo de português. O libtesseract-dev cria o link "libtesseract.so" que o Tess4J procura.
RUN apt-get update \
    && apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-por libtesseract-dev \
    && rm -rf /var/lib/apt/lists/*

ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

# Não rodar como root: se alguém explorar a aplicação, não ganha o container inteiro.
RUN useradd --system --uid 1001 app
USER app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
