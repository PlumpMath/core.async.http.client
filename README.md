# core.async.http.client

core.async.http.client is an HTTP library wrapping async-http-client.
It uses core.async channels to return the response.

[![Build Status](https://travis-ci.org/danpersa/core.async.http.client.svg?branch=master)](https://travis-ci.org/danpersa/core.async.http.client)

## Installation

core.async.http.client is available from [Clojars](https://clojars.org/groups/core.async.http.client)

With Leiningen/Boot:

```
[core.async.http.client "0.1.0-SNAPSHOT"]
```

## GET

```
(http/get "http://site.com/resources/id")
```

## Run Cucumber from IntelliJ
```
Main Class: cucumber.api.cli.Main
Glue: features/step_definitions
Feature Folder: .... core.async.http.client/features (Use full path)
VM Options: -Xbootclasspath/p:features/step_definitions:src:spec/resources:spec
Program Arguments: "--plugin" "pretty"
```

## License

Released under the [MIT License](http://www.opensource.org/licenses/mit-license.php)
