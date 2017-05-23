#! /bin/bash
### BEGIN INIT INFO
# Provides:          Queue Monitoring Tool 
# Required-Start:    $all
# Required-Stop:     $all
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Starts QMA
# Description:       Starts QMA using start-stop-daemon
### END INIT INFO

# You may need to change these
#JAVA_HOME=/usr/local/java   # Where java lives

if [ -z "${QMA_HOME}"  ]; then
	cd `dirname $0`
	QMA_HOME=$PWD/..
	export QMA_HOME
fi
	

### BEGIN user-configurable settings
NAME=QMA
PID_FILE=$QMA_HOME/$NAME.pid
LOG_DIR=$QMA_HOME/logs
QMA_MIN_MEM=512m
QMA_MAX_MEM=1g
DAEMON_OPTS="-Xms${QMA_MIN_MEM} -Xmx${QMA_MAX_MEM} "
### END user-configurable settings

# Exit if the executable is missing.
if [ ! -x $DAEMON ]; then
  echo 'Could not find QMA executable!'
  exit 0
fi

# Exit if any command (outside a conditional) fails.
set -e


case "$1" in
  start)
    echo -n "Starting Queue Monitoring Tool (QMA)..: "
    #nohup java -Xms512m -Xmx1024m -jar qpc.jar </dev/null 2>&1 & echo $! > queue-monitoring.pid
	#tee $HOME/qpc/logs/qpc.log &
    #nohup java $DAEMON_OPTS -jar ${QMA_HOME}/bin/qma.jar </dev/null 2>&1 & echo $! > ${PID_FILE}
	java $DAEMON_OPTS -jar ${QMA_HOME}/bin/qma.jar > ${LOG_DIR}/qma.log 2>&1 & 
	echo $! > ${PID_FILE}
    if [ $? == 0 ]
    then
        echo "started."
    else
        echo "failed."
    fi
    ;;
  stop)
    if [ ! -e $PID_FILE ]; then
      echo "Queue Monitoring Tool (QMA) not running (no PID file)"
    else
      echo -n "Stopping Queue Monitoring Tool (QMA): "
      kill $(cat $PID_FILE)
      rm -Rf $PID_FILE
      if [ $? == 0 ]
      then
          echo "stopped."
      else
          echo "failed."
      fi
    fi
    ;;
  restart|force-reload)
    ${0} stop
    sleep 0.5
    ${0} start
    ;;
  status)
    if [ ! -f $PID_FILE ]; then
      echo "Queue Monitoring Tool (QMA) not running"
    else
      if ps auxw | grep $(cat $PID_FILE) | grep -v grep > /dev/null; then
        echo "running on pid $(cat $PID_FILE)"
      else
        echo 'not running (but PID file exists)'
      fi
    fi
    ;;
  *)
    N=/etc/init.d/$NAME
    echo "Usage: $N {start|stop|restart|force-reload|status}" >&2
    exit 1
    ;;
esac

exit 0