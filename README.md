[hugo]: https://github.com/HugoCarvalho99
[hugo-pic]: https://github.com/HugoCarvalho99.png?size=120
[nelson]: https://github.com/nelsonmestevao
[nelson-pic]: https://github.com/nelsonmestevao.png?size=120
[pedro]: https://github.com/pedroribeiro22
[pedro-pic]: https://github.com/pedroribeiro22.png?size=120
[rui]: https://github.com/ruimendes29
[rui-pic]: https://github.com/ruimendes29.png?size=120

<div align="center">
    <img src="img/logo.png" alt="File Share" width="400px">
</div>

> A distributed file sharing system

## :rocket: Getting Started [^1]

[^1]: You can read more about the project goals in our [wiki](https://gitlab.com/mieiuminho/SD/fileshare/-/wikis).

Start by copying the `.env.sample` into `.env` and fill in the fields correctly.

```bash
cp .env.sample .env
```

This project uses settings configured in environment variables defined in the
`.env` file. In order to get those properly exported is recommend to set up
[direnv](https://direnv.net/) for a terminal based work flow and the plugin
[EnvFile](https://github.com/Ashald/EnvFile) for IntelliJ.

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

### :hammer_and_wrench: Tools

The recommended Integrated Development Environment (IDE) is IntelliJ IDEA.

## :busts_in_silhouette: Team

[![Hugo][hugo-pic]][hugo] | [![Nelson][nelson-pic]][nelson] | [![Pedro][pedro-pic]][pedro] | [![Rui][rui-pic]][rui]
:---: | :---: | :---: | :---:
[Hugo Carvalho][hugo] | [Nelson Estevão][nelson] | [Pedro Ribeiro][pedro] | [Rui Mendes][rui]


