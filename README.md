master: [![Build Status](https://travis-ci.org/CleverCloud/warp10-scala-client.svg?branch=master)](https://travis-ci.org/CleverCloud/warp10-scala-client)

# This is a scala client for [Warp10 Geo/time series DB](http://www.warp10.io/)

It is based on the [http4s](http://http4s.org/) client, and the Pooled HTTP client, keeping TCP connection alive. Configuration of the Client is [here](http://http4s.org/v0.15/api/#org.http4s.client.blaze.BlazeClientConfig)

## Features

This client is able to send data to the warp10 DB ATM. It's the main purpose and usage for the time being, on my side there is no active work on fetching data, PR welcome.

# How to add to your project

Add the dependency to your `build.sbt`:

    "com.clevercloud" %% "warp10-scala-client" % "2.0.1"

Add the resolver:

    resolvers += "Clever Cloud Bintray" at "https://dl.bintray.com/clevercloud/maven"

[ ![Download](https://api.bintray.com/packages/clevercloud/maven/warp10-scala-client/images/download.svg) ](https://bintray.com/clevercloud/maven/warp10-scala-client/_latestVersion) You can add yourself to the watchers list on the Bintray repo to be notified of
new versions: https://bintray.com/clevercloud/maven/warp10-scala-client


# Code examples

    val w10client = new Warp10Client(Uri.uri("http://localhost:8080/"), "WRITE")
    w10client.sendData(Warp10Data(time, None, "org.test.plain.string", Set("label1" -> "dsfF3", "label2" -> "dsfg"), "data string test")).run


Look at the tests directory for functional tests and code examples
