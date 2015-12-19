FROM ubuntu:15.10
RUN apt-get -y update \
	&& apt-get upgrade -y \
	&& apt-get install mongodb-server -y \
	&& apt-get install openjdk-8-jre -y \
	&& apt-get install nginx -y
ADD ./nginx-scampiRest /etc/nginx/sites-available/default
ADD ./scampiREST-1.0-SNAPSHOT.jar /usr/scampiREST-1.0-SNAPSHOT.jar
RUN chown www-data:root /usr/scampiREST-1.0-SNAPSHOT.jar
RUN service nginx reload
