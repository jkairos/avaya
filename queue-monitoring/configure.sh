#! /bin/bash
#
#
echo "Generating Queue Monitoring Jar File With All The Dependencies"
mvn package

export QMA_HOME=${HOME}/qma
echo "Creating Application Folder at ${QMA_HOME}"
rm -Rf ${QMA_HOME}
mkdir ${QMA_HOME}

echo "Setting Up Application"
mkdir ${QMA_HOME}/bin
mkdir ${QMA_HOME}/config
mkdir ${QMA_HOME}/contracts
mkdir ${QMA_HOME}/res_queue
mkdir ${QMA_HOME}/res_queue_adv_app_support
mkdir ${QMA_HOME}/res_queue_adv_app_imp
mkdir ${QMA_HOME}/res_pending_adv_app_support
mkdir ${QMA_HOME}/res_pending_adv_app_imp
mkdir ${QMA_HOME}/res_overdue
mkdir ${QMA_HOME}/res_pending
mkdir ${QMA_HOME}/db
mkdir ${QMA_HOME}/templates
mkdir ${QMA_HOME}/logs

cp -fr target/classes/contracts/*.* ${QMA_HOME}/contracts
cp -fr target/classes/templates/*.* ${QMA_HOME}/templates
cp -fr target/classes/db/*.sql ${QMA_HOME}/db
cp -fr target/classes/*.xml ${QMA_HOME}/config
cp -fr target/classes/config/prod/*.xml ${QMA_HOME}/config
cp -fr target/classes/config/prod/*.properties ${QMA_HOME}/config
cp -fr target/qma.jar ${QMA_HOME}/bin
cp -fr queue-monitoring.sh ${QMA_HOME}/bin
chmod a+x ${QMA_HOME}/bin/queue-monitoring.sh

echo "Deleting properties and XML files inside ${QMA_HOME}/bin/qma.jar file"
zip -d ${QMA_HOME}/bin/qma.jar applicationContext.xml
#zip -d ${QMA_HOME}/bin/qma.jar config.properties
zip -d ${QMA_HOME}/bin/qma.jar config/*
zip -d ${QMA_HOME}/bin/qma.jar contracts/*
zip -d ${QMA_HOME}/bin/qma.jar res/*
zip -d ${QMA_HOME}/bin/qma.jar db/*
zip -d ${QMA_HOME}/bin/qma.jar templates/*

echo "Setup Completed Successfully!!! Please Go To ${QMA_HOME}/bin and RUN ./queue-monitoring.sh start to Start the Queue Monitoring Application"