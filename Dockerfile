# ========================================================
# Production Dockerfile for MiniProject-DocPortal
# Works on Render, Railway, Koyeb, Fly.io, or any Docker host
# ========================================================

# Step 1: Build & Compile
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN mkdir -p sample/build/classes && \
    javac -cp "sample/src/main/webapp/WEB-INF/lib/*:sample/build/classes" -d sample/build/classes \
    sample/src/main/java/conn/DBConnection.java \
    sample/src/main/java/*.java

# Step 2: Lightweight Runtime Image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app .

EXPOSE 8080
ENV PORT=8080

CMD ["java", "-cp", "sample/src/main/webapp/WEB-INF/lib/*:sample/build/classes", "AppServer"]
