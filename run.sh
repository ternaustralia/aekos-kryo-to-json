#!/usr/bin/env bash
cd `dirname "$0"`
USAGE_MSG="  Usage: $0 <user> <password>"
user=$1
pass=$2
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
shift
shift
fetchSize=0
chunkSize=1000
args="--kryo-crawler.jdbc.url=jdbc:postgresql:aekos"
args="$args,--kryo-crawler.jdbc.user=$user"
args="$args,--kryo-crawler.jdbc.password=$pass"
args="$args,--kryo-crawler.jdbc.fetch-size=$fetchSize"
args="$args,--kryo-crawler.chunk-size=$chunkSize"
./mvnw clean spring-boot:run \
 -Drun.arguments=$args \
 $@

