# scampiREST

[![Build Status](https://drone.io/github.com/elias-arnold/scampiREST/status.png)](https://drone.io/github.com/elias-arnold/scampiREST/latest)


<h1>Framework:</h1> 
http://www.liberouter.mobi/


Design Diagrams:

Built new Rest Message from Scampi: 
https://creately.com/diagram/ihnap9eq3/rgxMpRw4AMT5vlVSxi9BLBzJU%3D

Publish new Message to Scampi:
https://creately.com/diagram/ihn6w04q/HC4OKwFY3QlDbjs2l2NNiN8k9iU%3D

Request new Service from Scampi:
https://creately.com/diagram/ihm7ybd31/quPHInTgBHPyNNbzjHlpRcWDtAw%3D



Curl Commands to test and interact with the API:

Request all Messages for a service:
curl -H "Accept: application/json" http://localhost:8080/dyn/service/myservice

Get a empty message:
curl -H "Accept: application/json" http://localhost:8080/dyn/message/empty

Stage a new message: 
curl -X POST -H "Content-Type:application/json" http://localhost:8080/dyn/message/stage -d '{"id":null,"appTag":"","service":"myservice","stringMap":{"testKey":"testValue"},"integerMap":{},"floatMap":{},"binaryMap":{},"metaData":{}}'

Pubish a staged message:
curl -H "Accept: application/json" http://localhost:8080/dyn/message/publish/$myid
