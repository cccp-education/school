FROM gradle:jdk21-alpine AS build
COPY . .
RUN gradle -s -q -p workspace-model :build -x test; gradle -s -q -p api :build -x test;

FROM openjdk:jre-alpine
COPY --from=build /api/build/libs/api-0.0.1.jar school-api.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","school-api.jar"]

