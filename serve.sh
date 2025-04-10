#!/usr/bin/bash

if [[ $BASH_SOURCE = */* ]]; then
    DIR=${BASH_SOURCE%/*}/
else
    DIR=./
fi

exec java -Xmx256M -jar "$DIR/target/raft-1.0-SNAPSHOT-jar-with-dependencies.jar"