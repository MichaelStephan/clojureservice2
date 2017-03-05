FROM java:8
VOLUME /tmp
ADD target/app.jar app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 6667 
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
