# Project Title

One Paragraph of project description goes here

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

What things you need to install the software and how to install them

```
Give examples
```

### Installing

Run the folowing commands from your project root after creating your own s3 bucket.  

Build the project using maven
```
mvn package  
```

Package and deploy web apis

```
aws cloudformation package --template-file webapi.yaml --output-template-file output-webapi.yaml --s3-bucket octank-healthcare  
aws cloudformation deploy --template-file output-webapi.yaml --stack-name OctankClaimsWebApis  
```

Package and deploy backsync lambda

```
aws cloudformation package --template-file backsync.yaml --output-template-file output-backsync.yaml --s3-bucket octank-healthcare  
aws cloudformation deploy --template-file output-backsync.yaml --stack-name OctankClaimsBackSync  
```



## Running the tests

 

### Break down into end to end tests



### And coding style tests



## Deployment

## Contributing

## Authors

* **Ram Vittal** - *Initial work* - [RamVittal](https://github.com/ramvittal)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments
