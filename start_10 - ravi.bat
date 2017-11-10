cd BS
start cmd /k "start.bat"
sleep 2
cd..


start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10001 9001 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10002 9002 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10003 9003 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10004 9004 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10005 9005 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10006 9006 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10007 9007 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10008 9008 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10009 9009 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10010 9010 crystal REST"




