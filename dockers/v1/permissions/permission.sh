#!/bin/bash
AUTH_FILE=/tmp/permissions-auth.json
AUDIT_FILE=/tmp/permissions-audit.json
BACK_FILE=/tmp/permissions-olap-x.json
if [ -f "$AUTH_FILE" ]; then
    echo "$AUTH_FILE exists."
    /usr/local/PeykAsa/auth-server/permission-importer.sh -s auth -a update -l info -f $AUTH_FILE
    mv $AUTH_FILE $AUTH_FILE.imported
else
    echo "$AUTH_FILE does not exist."
fi
if [ -f "$BACK_FILE" ]; then
    echo "$BACK_FILE exists."
    /usr/local/PeykAsa/auth-server/permission-importer.sh -s back-end -a update -l info -f $BACK_FILE
    mv $BACK_FILE $BACK_FILE.imported
else
    echo "$BACK_FILE does not exist."
fi
if [ -f "$AUDIT_FILE" ]; then
    echo "$AUDIT_FILE exists."
    /usr/local/PeykAsa/auth-server/permission-importer.sh -s audit -a update -l info -f $AUDIT_FILE
    mv $AUDIT_FILE $AUDIT_FILE.imported
else
    echo "$AUDIT_FILE does not exist."
fi
