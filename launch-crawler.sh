#!/bin/bash
cd `dirname "$0"`
USAGE_MSG="  Usage: $0 <user> <password>"
LOGS_DIR=logs
UNIQUE_ID=`date +%Y%m%d_%H%M`
mkdir -p $LOGS_DIR

user=$1
pass=$2
url=jdbc:postgresql:aekos # TODO make param
if [ -z "$user" ];then
  echo "[ERROR] you need to pass the DB username as the first arg"
  echo $USAGE_MSG
  exit 1
fi
if [ -z "$pass" ];then
  echo "[ERROR] you need to pass the DB password as the second arg"
  echo $USAGE_MSG
  exit 1
fi

#$HEAP_DUMP_FRAGMENT=-XX:+HeapDumpOnOutOfMemoryError
#DEBUG_FRAGMENT="-agentlib:jdwp=suspend=y,server=y,transport=dt_socket,address=8000"
jar=`find target/ -name "kryo-to-json*.jar" -not -name "*sources*"`
if [ -z $jar ]; then
  echo "[ERROR] couldn't find JAR file, exiting"
  exit 1
fi
fetchSize=5000
chunkSize=1000

java -Xms256m -Xmx5G $HEAP_DUMP_FRAGMENT $DEBUG_FRAGMENT -noverify \
 -jar $jar \
 --kryo-crawler.jdbc.url="$url" \
 --kryo-crawler.jdbc.user="$user" \
 --kryo-crawler.jdbc.password="$pass" \
 --kryo-crawler.jdbc.fetch-size="$fetchSize" \
 --kryo-crawler.chunk-size="$chunkSize" \
 2>&1 | tee $LOGS_DIR/kryo_crawler_$UNIQUE_ID.log

