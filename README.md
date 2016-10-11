# DDegenerate

Data Dictionary generator.

## Usage

### Complie
To compile ddgenerate :

```sh
$ cd ddgenerate/
$ mvn clean compile assembly:single
```

### Run
To run ddgenerate :

```sh
$ java -jar target/ddgenerate-1.0-SNAPSHOT-jar-with-dependencies.jar "<path_al_diccionario>/diccionario.xlsx" -f <format> -o <path_output>
```

Example :

```sh
$ java -jar target/ddgenerate-1.0-SNAPSHOT-jar-with-dependencies.jar "../diccionario.xlsx" -f datapackage -o ../client/

```

The ouptut will be in `client`

## Want to contribute?

If you've found a bug or have a great idea for new feature let me know by [adding your suggestion]
(http://github.com/mbaez/ddgenerate/issues/new) to [issues list](https://github.com/mbaez/ddgenerate/issues).
