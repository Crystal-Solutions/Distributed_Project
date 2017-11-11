
cd BS
start cmd /k "start.bat"
cd ..
sleep 2

start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10001 9001 crystal UDP"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10002 9002 crystal UDP"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10003 9003 crystal UDP"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10004 9004 crystal UDP"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10005 9005 crystal UDP"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10006 9006 crystal UDP"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10007 9007 crystal UDP"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10008 9008 crystal UDP"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10009 9009 crystal UDP"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10010 9010 crystal UDP"

cd front_end
start cmd /k "start.bat"
