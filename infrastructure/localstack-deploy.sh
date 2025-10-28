#!/bin/bash

set -e # stops the script if a command fails

aws --endpoint-url=http://localhost:4566 cloudformation deploy \
    --stack-name hotel-booking-system \
    --template-file "infrastructure/cdk.out/localStack.template.json"

aws --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers \
    --query "LoadBalancers[0].DNSName" --output text
