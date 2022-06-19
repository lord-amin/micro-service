#!/bin/sh

COMPONENT_NAME=@project.artifactId@
NAME=$COMPONENT_NAME-@project.version@
SERVICE_NAME=pa-$COMPONENT_NAME
echo "component name $COMPONENT_NAME"
echo "name $NAME"
echo "service name $SERVICE_NAME"
echo "making directory /usr/local/PeykAsa/$COMPONENT_NAME/"
mkdir -p /usr/local/PeykAsa/$COMPONENT_NAME/log/
echo "copying '$NAME.jar' file to /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.jar"
cp $NAME.jar /usr/local/PeykAsa/$COMPONENT_NAME/
echo "copying '$NAME.conf' file to /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.conf"
#cp $NAME.conf /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.conf
echo "copying config directory to /usr/local/PeykAsa/$COMPONENT_NAME/config"
cp -r ./config /usr/local/PeykAsa/$COMPONENT_NAME/
echo "copying /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.jar to /usr/local/PeykAsa/$COMPONENT_NAME/app.jar"
cp /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.jar /usr/local/PeykAsa/$COMPONENT_NAME/app.jar
#echo "copying /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.conf /usr/local/PeykAsa/$COMPONENT_NAME/app.conf"
#cp /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.conf /usr/local/PeykAsa/$COMPONENT_NAME/app.conf
echo "remove /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.jar"
rm /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.jar
#echo "remove /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.conf"
#rm /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.conf
echo "install completed: "
echo ""
#echo "service config file: /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.conf"
echo "config path:         /usr/local/PeykAsa/$COMPONENT_NAME/config"
echo "log path:            /usr/local/PeykAsa/$COMPONENT_NAME/log"
echo ""

