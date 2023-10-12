FROM amazoncorretto:17

COPY ./build/libs/backend.jar ./backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
