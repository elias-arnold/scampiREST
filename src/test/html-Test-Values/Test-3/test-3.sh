serviceName=$1
echo -e "\nstage empty message"
muhh='{"appTag":null,"service":"'$serviceName'","stringMap":{"name":"asdasd-asdasd"},"integerMap":{"testIntKey":1},"floatMap":{},"binaryMap":{},"metaData":{}}'

echo $muhh

myid=$(curl -X POST -H "Content-Type:application/json" http://localhost/dyn/message/stage -d $muhh)

echo -e "\n\n\n$myid\n\n\n"
read myid
echo -e "\nupload a index zip"
curl -v -F name="scampiRest-picture-demo" -F key="main" -F id="$myid" -F file=@"./scampiRest-picture-demo.zip" http://localhost/dyn/upload
echo -e "\nupload sucessfull"

echo -e "\nupload a index zip"
curl -v -F name="yoga-girl.jpeg" -F key="picture" -F id="$myid" -F file=@"./yoga-girl.jpeg.zip" http://localhost/dyn/upload
echo -e "\nupload sucessfull"

echo -e "\npublish the message"
curl -H "Accept: application/json" http://localhost/dyn/message/publish/$myid
echo -e "\npublish sucessful"

curl -H "Accept: application/json" http://localhost/dyn/subscribe/$serviceName

echo -e "\nget service"
curl -H "Accept: application/json" http://localhost/dyn/service/$serviceName

