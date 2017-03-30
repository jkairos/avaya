#!/bin/ksh
#
#
nohup java -Xms512m -Xmx1024m -jar qpc.jar </dev/null 2>&1 & echo $! > queue-monitoring.pid
tee $HOME/qpc/logs/qpc.log &