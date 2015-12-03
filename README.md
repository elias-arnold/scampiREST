# scampiREST

[![Build Status](https://drone.io/github.com/elias-arnold/scampiREST/status.png)](https://drone.io/github.com/elias-arnold/scampiREST/latest)


<h2>Framework:</h2> 
http://www.liberouter.mobi/


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
