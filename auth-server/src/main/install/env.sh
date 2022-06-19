#!/bin/bash
RUN_ARGS="--logging.config=file:./config/log4j2.xml --spring.config.location=./config/ "
if [ -n "$JVM_MIN_MEM" ]; then
  a=" -Xms$JVM_MIN_MEM"
  JAVA_OPTS="${JAVA_OPTS} ${a}"
fi
if [ -n "$JVM_MAX_MEM" ]; then
  a=" -Xmx$JVM_MAX_MEM"
  JAVA_OPTS="${JAVA_OPTS} ${a}"
fi
#if [ -n "$SERVER_PORT" ]; then
#  a=" --server.port=$SERVER_PORT"
#  RUN_ARGS="${RUN_ARGS} ${a}"
#fi
#if [ -n "$MYSQL_URL" ]; then
#  a=" --spring.datasource.url=$MYSQL_URL"
#  RUN_ARGS="${RUN_ARGS} ${a}"
#fi
#if [ -n "$MYSQL_USER" ]; then
#  a=" --spring.datasource.username=$MYSQL_USER"
#  RUN_ARGS="${RUN_ARGS} ${a}"
#fi
#if [ -n "$MYSQL_PASS" ]; then
#  a=" --spring.datasource.password=$MYSQL_PASS"
#  RUN_ARGS="${RUN_ARGS} ${a}"
#fi
#if [ -n "$SHOW_SQL" ]; then
#  a=" --spring.jpa.show-sql=$SHOW_SQL"
#  RUN_ARGS="${RUN_ARGS} ${a}"
#fi
#if [ -n "$APP_AUDIT_REMOTE_DEST" ]; then
#  a=" --app.audit.remote.dest=$APP_AUDIT_REMOTE_DEST"
#  RUN_ARGS="${RUN_ARGS} ${a}"
#fi
#if [ -n "$ADMIN_AUDIT_ENABLED" ]; then
#  a=" --app.audit.admin.enabled=$ADMIN_AUDIT_ENABLED"
#  RUN_ARGS="${RUN_ARGS} ${a}"
#fi
#if [ -n "$ADMIN_VIEW_ENABLED" ]; then
#  a=" --app.admin.view.enabled=$ADMIN_VIEW_ENABLED"
#  RUN_ARGS="${RUN_ARGS} ${a}"
#fi
echo "--- \"$RUN_ARGS\" ----"
echo "--- \"$JAVA_OPTS\" ----"
conf=`ls @project.artifactId@-*.conf | sed 's/.conf//'`
echo "--- conf file is $conf ----"
echo "" > $conf.conf
echo "JAVA_OPTS=\"$JAVA_OPTS\"" >> $conf.conf
echo "RUN_ARGS=\"$RUN_ARGS\"" >> $conf.conf
