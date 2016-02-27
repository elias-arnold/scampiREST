serviceName=$1
echo -e "\nstage empty message"
muhh='{"appTag":null,"service":"'$serviceName'","stringMap":{"name":"testname"},"integerMap":{"testIntKey":1},"floatMap":{},"binaryMap":{},"metaData":{}}'

echo $muhh

myid=$(curl -X POST -H "Content-Type:application/json" http://myliberouter.org/dyn/message/stage -d $muhh)

echo -e "\n\n\n$myid\n\n\n"
