curl -H "Accept: application/json" http://localhost:8080/dyn/message/empty  
myid=$(curl -X POST -H "Content-Type:application/json" http://localhost/dyn/message/stage -d '{"appTag":null,"service":"myservice","stringMap":{"testKey":"testValue"},"integerMap":{},"floatMap":{},"binaryMap":{},"metaData":{}}')
curl -H "Accept: application/json" http://localhost/dyn/message/publish/$myid
curl -H "Accept: application/json" http://localhost/dyn/service/myservice
curl -H "Accept: application/json" http://localhost/dyn/subscribe/myservice

curl -v -F name=somename -F upload=@"./" URL
