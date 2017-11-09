start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10001 9001 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10002 9002 crystal REST"
start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 localhost 10003 9003 crystal REST"

cd BS
start cmd /k "start.bat"

cd ..\front_end
start cmd /k "start.bat"
