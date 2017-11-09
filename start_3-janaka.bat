start cmd /k "BS\start.bat"

start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 192.168.8.102 10001 9001 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 192.168.8.102 10002 9002 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 192.168.8.102 10003 9003 crystal REST"


cd ..\front_end
start cmd /k "start.bat"
