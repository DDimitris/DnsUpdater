#!/usr/bin/env bash

bash compile.sh

mkdir /etc/dnsUpdater
mkdir /etc/dnsUpdater/log

cp ../target/dnsUpdater-1.0-SNAPSHOT-jar-with-dependencies.jar /etc/dnsUpdater

cp ../src/main/resources/dns_server.config /etc/dnsUpdater

cp dnsDaemon /etc/init.d/

sudo chmod 755 /etc/init.d/dnsDaemon
sudo chown root:root /etc/init.d/dnsDaemon