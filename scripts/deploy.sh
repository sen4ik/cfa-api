#!/bin/bash

if [[ -z $4 ]] ; then
    echo "USAGE: $0 APP DEPLOY_SERVER_ADDRESS APP_PORT JWT_SECRET [optional: DB_NAME_SUFFIX]"
    exit 1
fi

APP=$1; shift
DEPLOY_SERVER_ADDRESS=$2; shift
APP_PORT=$3; shift
JWT_SECRET=$4; shift
OPTIONS="$*"

set -e

CFA_API_LINK=/etc/init.d/$APP
APP_PROP=$(pwd)/../src/main/resources/application.properties

if [ -L ${CFA_API_LINK} ] ; then
   if [ -e ${CFA_API_LINK} ] ; then
      echo $CFA_API_LINK" link is good."
   else
      echo $CFA_API_LINK" link is broken."
      exit 1
   fi
elif [ -e ${CFA_API_LINK} ] ; then
   echo $CFA_API_LINK" is not a link."
   exit 1
else
   echo $CFA_API_LINK" is missing."
   exit 1
fi

$CFA_API_LINK stop && \
sleep 5 && \
cd cd $(pwd)/../ && \
git fetch origin && \
git reset --hard origin/master

if [[ ! -z $5 ]] ; then
    OGIRINAL_DB_NAME=$(awk '/spring.datasource.url/{print $NF}' "$APP_PROP" | awk -F'/' '{print $4}' | awk -F'?' '{print $1}')
    sed -i -e "s/$OGIRINAL_DB_NAME/$OGIRINAL_DB_NAME$5/g" "$APP_PROP"
fi

sed -i "/server.host=/c\server.host=$DEPLOY_SERVER_ADDRESS" "$APP_PROP" && \
sed -i "/server.port=/c\server.port=$APP_PORT" "$APP_PROP" && \
sed -i "/spring.devtools.restart.enabled=/c\spring.devtools.restart.enabled=false" "$APP_PROP" && \
sed -i "/security.jwt.token.secret-key=/c\security.jwt.token.secret-key=$JWT_SECRET" "$APP_PROP" && \
sed -i "/file.upload-dir=/c\file.upload-dir=$(pwd)/files/" "$APP_PROP" && \
./scripts/package.sh && \
$CFA_API_LINK start
echo "Wait 30 seconds for API to start..."
sleep 30
RESPONSE_CODE=$(curl -sL -w "%{http_code}\\n" "https://$DEPLOY_SERVER_ADDRESS/api/v1/category/all" -o /dev/null)
if [[ ! "$RESPONSE_CODE" == 200 ]]; then echo "Wasn't able to verify API is up!" && exit 1; fi
