serviceName=$1
echo -e "\nstage empty message"
muhh='{"appTag":null,"service":"'$serviceName'","stringMap":{"name":"testname"},"integerMap":{"testIntKey":1},"floatMap":{},"binaryMap":{},"metaData":{}}'

echo $muhh

myid=$(curl -X POST -H "Content-Type:application/json" http://myliberouter.org/dyn/message/stage -d $muhh)

echo -e "\n\n\n$myid\n\n\n"
read myid
echo -e "\nupload a index zip"
curl -v -F name="scampiRest-picture-demo" -F key="main" -F id="$myid" -F file=@"./scampiRest-picture-demo.zip" http://myliberouter.org/dyn/upload
echo -e "\nupload sucessfull"

echo -e "\nupload a index zip"
curl -v -F name="Hamlet-1.jpeg" -F key="picture" -F id="$myid" -F file=@"./Hamlet-1.jpg.zip" http://myliberouter.org/dyn/upload
echo -e "\nupload sucessfull"

echo -e "\nupload a index zip"
curl -v -F name="Emilio-1.jpeg" -F key="picture2" -F id="$myid" -F file=@"./Emilio-1.jpg.zip" http://myliberouter.org/dyn/upload
echo -e "\nupload sucessfull"

echo -e "\npublish the message"
curl -H "Accept: application/json" http://myliberouter.org/dyn/message/publish/$myid
echo -e "\npublish sucessful"

curl -H "Accept: application/json" http://myliberouter.org/dyn/subscribe/$serviceName

echo -e "\nget service"
curl -H "Accept: application/json" http://myliberouter.org/dyn/service/$serviceName

