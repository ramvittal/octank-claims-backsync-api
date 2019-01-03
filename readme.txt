
old:
   1. Create a package: mvn package

   2. Upload to S3: mvn install

   3. Deploy Lambda: aws cloudformation deploy --template-file backsync.yaml --stack-name claimsbacksync
   
   aws cloudformation deploy --template-file webapi.yaml --stack-name OctankClaimsWebApis
   
   3b. aws lambda update-function-code --function-name ClaimsBacksync --s3-bucket octank-healthcare --s3-key octank-claims-backsync-1.0-SNAPSHOT.jar

   4. Invoke Lambda:

   5. aws lambda invoke \
    --function-name ClaimsBacksync \
    --payload '{ "requestId": "batch-claims", "claimStatus": "Submitted" }' \
    claims.out

   6. Delete Lambda: aws cloudformation delete-stack --stack-name claimsbacksync

New:

mvn package

aws cloudformation package --template-file webapi.yaml --output-template-file output-webapi.yaml --s3-bucket octank-healthcare
aws cloudformation deploy --template-file output-webapi.yaml --stack-name OctankClaimsWebApis
