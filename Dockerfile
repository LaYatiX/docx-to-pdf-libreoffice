FROM ubuntu

RUN apt-get update && \
    apt-get install -y wget gpg
RUN wget -O - https://apt.corretto.aws/corretto.key | gpg --dearmor -o /usr/share/keyrings/corretto-keyring.gpg
RUN echo "deb [signed-by=/usr/share/keyrings/corretto-keyring.gpg] https://apt.corretto.aws stable main" | tee /etc/apt/sources.list.d/corretto.list
RUN apt-get update; apt-get install -y java-17-amazon-corretto-jdk

ENV JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto-jdk

ENV PATH="$PATH:$JAVA_HOME/bin"
RUN apt install -y libreoffice-core-nogui
RUN apt install -y libreoffice-writer

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
RUN mkdir -p /app/files

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "/app.jar"]