#!/usr/bin/env bash

echo "Compiling project..."
workingDir=`pwd`
cd ..
mvn clean package
cd $workingDir
echo "Compilation finished!"
