#!/usr/bin/env sh

old_PID=`ps -eaf | grep nfc-service.jar | tr -s " " | cut -d " " -f2`
kill -9 ${old_PID}

SPRING_PROFILE=${APP_ENV}
if [ -z "${SPRING_PROFILE}" ]
then
  SPRING_PROFILE=prod
fi

java -jar -Duser.timezone=UTC -Xmx2G  -Dspring.profiles.active=${SPRING_PROFILE} nfc-service.jar > /home/ninja/nfc-service/app.log &