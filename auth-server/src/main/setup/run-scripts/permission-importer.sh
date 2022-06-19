#!/bin/bash

COMPONENT_NAME=@project.artifactId@
NAME=$COMPONENT_NAME-@project.version@
SERVICE_NAME=pa-$COMPONENT_NAME
/etc/init.d/$SERVICE_NAME run --import $*
