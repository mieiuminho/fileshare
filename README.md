# MediaFile Share
> A distributed MediaFile sharing system

## :rocket: Getting Started

### :hammer: Development

Compile the project in a clean build.

```
mvn clean compile
```

Start the server.

```
mvn exec:java@server
```

Start a client.

```
mvn exec:java@client
```

Running tests.

```
mvn test
```

Format the code accordingly to common guide lines.

```
mvn formatter:format
```

Lint your code with _checkstyle_.

```
mvn checkstyle:check
```

### :package: Deployment

Bundling the app into jar file.

```
mvn package
```
