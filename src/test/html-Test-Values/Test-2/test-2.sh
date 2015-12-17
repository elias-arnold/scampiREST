echo -e "\nstage empty message"
myid=$(curl -X POST -H "Content-Type:application/json" http://localhost/dyn/message/stage -d '{"appTag":null,"service":"test2app","stringMap":{"testKey":"testValue"},"integerMap":{"testIntKey":1},"floatMap":{},"binaryMap":{},"metaData":{}}')

echo -e "\n\n\n$myid\n\n\n"

echo -e "\nupload a index zip"
curl -v -F name="somename" -F id="$myid" -F file=@"./scampiRest-picture-demo.zip" http://localhost/dyn/upload

echo -e "\nupload sucessfull"
echo -e "\npublish the message"
curl -H "Accept: application/json" http://localhost/dyn/message/publish/$myid
echo -e "\npublish sucessful"

curl -H "Accept: application/json" http://localhost/dyn/subscribe/myservice

echo -e "\nget service"
curl -H "Accept: application/json" http://localhost/dyn/service/myservice

