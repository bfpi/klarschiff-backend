#!/usr/bin/env sh

DIR=`dirname $0`

mvn install:install-file \
  -Dfile=$DIR/commons-regex-0.1.jar \
  -DgroupId=commons-regex \
  -DartifactId=commons-regex \
  -Dversion=0.1 \
  -Dpackaging=jar
