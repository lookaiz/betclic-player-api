#!/bin/bash

export DYNAMODB_ENDPOINT="http://localhost:4566"

echo "Build server"
./gradlew clean build

echo "Launch Ktor server using DynamoDB endpoint $DYNAMODB_ENDPOINT"
java -jar -DDYNAMODB_ENDPOINT=$DYNAMODB_ENDPOINT ./build/libs/betclic-tournament-api.jar
