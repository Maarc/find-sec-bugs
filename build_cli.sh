#!/bin/bash

rm -f cli/findsecbugs-cli-*.zip

mvn clean package -Dmaven.test.skip=true -DskipTests -pl "findsecbugs-plugin"

cd cli
gradle packageCli

cd ..
ZIP=$(echo cli/findsecbugs-cli-*.zip)

echo ""
echo "Built CLI: ${ZIP}"
