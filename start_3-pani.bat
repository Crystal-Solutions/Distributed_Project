REM start cmd /k "BS\start.bat"

start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main 192.168.8.106 55555 192.168.8.108 10012 9010 Pani UDP"
REM start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 192.168.8.102 10002 9002 crystal REST"
REM start cmd /k "java -cp target\client-project-1.0-SNAPSHOT-jar-with-dependencies.jar com.distributed.app.Main localhost 55555 192.168.8.102 10003 9003 crystal REST"


REM cd ..\front_end
REM start cmd /k "start.bat"
