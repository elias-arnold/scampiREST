# scampiREST

[![Build Status](https://drone.io/github.com/elias-arnold/scampiREST/status.png)](https://drone.io/github.com/elias-arnold/scampiREST/latest)


<h2>Framework:</h2> 
http://www.liberouter.mobi/

<h2>Run the Server:</h2>
<p>
<h3>Built from source:</h3>
This project is a using the Sping.io Boot framework. It has an embedded Tomcat Server included. Built the project utilizing maven with:
``` bash
mvn clean package -Dmaven.test.skip=true
``` 
And you will find your a runnable jar file under: ./target/
with the name: scampiREST-1.0-SNAPSHOT.jar

<h3>Run it with:</h3>

``` bash
sudo -u www-data java -Xms56m -Xmx128m -XX:PermSize=56m -XX:MaxPermSize=128m -jar ./scampiREST-1.0-SNAPSHOT.jar
``` 
www-data is a user. It is the same as the nginx user. You should not run the server as root!

<h3>Folders:</h3>
The server is creating two folders und /var/tmp and should have write permission there. 
* /var/tmp/nginxFiles
* /var/tmp/scampiRest

The logging is used to be under: 
* /var/tmp/scampiRest
For security reasons you might want to change the file spring.log which will be created only to write enabled with chmod 222 ./spring.log
</p>

<h2>Additional components requried:</h2>
* Nginx (http://nginx.org/)
* Mongodb (https://www.mongodb.org/)
* Scampi

<h2>Design Diagrams:</h2>

<h3>Built new Rest Message from Scampi:</h3>
https://creately.com/diagram/ihnap9eq3/rgxMpRw4AMT5vlVSxi9BLBzJU%3D

<h3>Publish new Message to Scampi:</h3>
https://creately.com/diagram/ihn6w04q/HC4OKwFY3QlDbjs2l2NNiN8k9iU%3D

<h3>Request new Service from Scampi:</h3>
https://creately.com/diagram/ihm7ybd31/quPHInTgBHPyNNbzjHlpRcWDtAw%3D


<h2>Curl Commands to test and interact with the API:</h2>

<h3>Request all Messages for a service:</h3>
``` bash
curl -H "Accept: application/json" http://localhost:8080/dyn/service/myservice
```
<h3>Get a empty message:</h3>
``` bash
curl -H "Accept: application/json" http://localhost:8080/dyn/message/empty
```
<h3>Stage a new message:</h3>
``` bash
curl -X POST -H "Content-Type:application/json" http://localhost:8080/dyn/message/stage -d '{"id":null,"appTag":"","service":"myservice","stringMap":{"testKey":"testValue"},"integerMap":{},"floatMap":{},"binaryMap":{},"metaData":{}}'
```
<h3>Pubish a staged message:</h3>
``` bash
curl -H "Accept: application/json" http://localhost:8080/dyn/message/publish{newMessageId}
```
