#!/bin/ksh
#
#

echo "Generating Queue Monitoring Jar File With All The Dependencies"
mvn clean assembly:assembly

echo "Creating Application Folder at $HOME/qpc"
rm -Rf $HOME/qpc
mkdir $HOME/qpc

echo "Setting Up Application"
mkdir $HOME/qpc/contracts
mkdir $HOME/qpc/res
mkdir $HOME/qpc/db
mkdir $HOME/qpc/templates
mkdir $HOME/qpc/logs

cp -fr target/classes/contracts/*.* $HOME/qpc/contracts
cp -fr target/classes/templates/*.* $HOME/qpc/templates
cp -fr target/classes/db/*.sql $HOME/qpc/db
cp -fr target/classes/*.properties $HOME/qpc
cp -fr target/classes/*.xml $HOME/qpc
cp -fr target/classes/config/prod/*.xml $HOME/qpc
cp -fr target/qpc-jar-with-dependencies.jar $HOME/qpc
cp -fr queue-monitoring.sh $HOME/qpc
chmod a+x $HOME/qpc/queue-monitoring.sh
zip -d $HOME/qpc/qpc-jar-with-dependencies.jar applicationContext.xml
zip -d $HOME/qpc/qpc-jar-with-dependencies.jar config.properties
zip -d $HOME/qpc/qpc-jar-with-dependencies.jar contracts/*
zip -d $HOME/qpc/qpc-jar-with-dependencies.jar res/*
zip -d $HOME/qpc/qpc-jar-with-dependencies.jar db/*
zip -d $HOME/qpc/qpc-jar-with-dependencies.jar templates/*

echo "Setup Completed Successfully!!! Please Go To $HOME/qpc and Run ./queue-monitoring.sh to Start the Queue Monitoring Application"