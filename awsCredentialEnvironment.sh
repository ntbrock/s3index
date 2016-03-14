#!/bin/sh
# The codeminder aws library refers back to environment variables to initialize AWSAuthenticated access to S3 buckets.

export AWS_ACCESS_KEY_ID="AKIA...."
export AWS_SECRET_ACCESS_KEY="8z......"
export AWS_SECRET_KEY=$AWS_SECRET_ACCESS_KEY
echo "$AWS_ACCESS_KEY_ID $AWS_SECRET_ACCESS_KEY"

# ./activator run
