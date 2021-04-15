#!/usr/bin/env bash

./mvnw package &&
libs/wtk/bin/emulator \
  -cp app/target/tmicro-1.0-SNAPSHOT-me.jar \
  -Xdescriptor:app/target/tmicro-1.0-SNAPSHOT-me.jad
