FROM openjdk:21.0.2-jdk-slim

COPY .out/artifacts/Server_Client_main_jar/Server_Client_main.jar /app/Server-Client-main.jar

COPY ./ps2023minmax.csv /app/ps2023minmax.csv

WORKDIR /app 
EXPOSE 4578

CMD ["java", "-jar", "Server-Client-main.jar"]

#docker build .

#docker run -p 4578:4578 <identificativo>