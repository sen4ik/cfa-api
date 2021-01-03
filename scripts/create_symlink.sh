#!/bin/bash
CFA_API_LINK=/etc/init.d/cfa-api

if [ -L ${CFA_API_LINK} ] ; then
   if [ -e ${CFA_API_LINK} ] ; then
      echo "Good link. Deleting."
      sudo $CFA_API_LINK stop && sleep 3
      sudo rm $CFA_API_LINK
   else
      echo "Broken link. Deleting."
      sudo $CFA_API_LINK stop && sleep 3
      sudo rm $CFA_API_LINK
   fi
elif [ -e ${CFA_API_LINK} ] ; then
   echo "Not a link."
else
   echo "Missing."
fi

CFA_JAR_NAME=cfa-api-0.0.1-SNAPSHOT.jar
echo "CFA_JAR_NAME: ${CFA_JAR_NAME}"

CFA_JAR_TARGET=../target/$CFA_JAR_NAME
echo "CFA_JAR_TARGET: ${CFA_JAR_TARGET}"

CFA_JAR_ABS_PATH=$(cd "$(dirname "$1")"; pwd -P)/target/$(basename "$CFA_JAR_TARGET")
echo "CFA_JAR_ABS_PATH: ${CFA_JAR_ABS_PATH}"

if [ ! -f "$CFA_JAR_ABS_PATH" ]; then
    echo "$CFA_JAR_ABS_PATH does not exists. Cant proceed. Exiting..."
    exit 1
fi

sudo chmod 500 $CFA_JAR_ABS_PATH
sudo ln -s $CFA_JAR_ABS_PATH $CFA_API_LINK
sudo $CFA_API_LINK start
