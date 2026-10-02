FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# バーコードの下に書く文字の描画に、フォント関係のライブラリが必要
RUN apt-get update && apt-get install -y --no-install-recommends libfreetype6 fontconfig libfontconfig1 \
    && rm -rf /var/lib/apt/lists/*

COPY . .

RUN ./gradlew --no-daemon build

CMD ["java", "-jar", "-Dserver.port=5000", "build/libs/barcodesample-0.0.1-SNAPSHOT.jar"]
