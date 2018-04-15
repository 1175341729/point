 echo parameter is $1 $2
PID=$(ps -ef | grep $1.jar | grep -v grep | awk '{ print $2 }')
if [ -z "$PID" ]
then
    echo Application is already stopped
else
    echo kill $PID
    kill -9 $PID
    echo stop successed
fi

cp /data/$1/temp/$1.jar /data/$1

java -jar /data/$1/$1.jar --server.port=$2  >/dev/null &
