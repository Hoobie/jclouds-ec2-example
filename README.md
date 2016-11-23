# How to run

## Configuration
You have to provide a `Configuration.properties` file and put it into the `resources` dir.

### Configuration file example
```
aws.credentials.accessKeyId=<access_key>
aws.credentials.secretAccessKey=<secret_key>
aws.region=eu-west-1
aws.imageId=ami-1234
```

## Running with Gradle
JDK 1.8+ is required.

`./gradlew execute`
