master: [![Build Status](https://travis-ci.org/CleverCloud/warp10-scala-client.svg?branch=master)](https://travis-ci.org/CleverCloud/warp10-scala-client)

# This is a scala client for [Warp10 Geo/time series DB](http://www.warp10.io/)

It is based on AKKA Stream.

## Features

Send and retrieve data are working using `fulltext` format. PR welcome.

# How to add to your project

Add the dependency to your `build.sbt`:

`"com.clevercloud" %% "warp10-scala-client" % "3.0.0"`

Add the resolver:

`resolvers += "Clever Cloud Bintray" at "https://dl.bintray.com/clevercloud/maven"`

[ ![Download](https://api.bintray.com/packages/clevercloud/maven/warp10-scala-client/images/download.svg) ](https://bintray.com/clevercloud/maven/warp10-scala-client/_latestVersion) You can add yourself to the watchers list on the Bintray repo to be notified of
new versions: https://bintray.com/clevercloud/maven/warp10-scala-client

# Code examples

```
import akka.actor._
import akka.stream.ActorMaterializer

import com.clevercloud.warp10client._
import com.clevercloud.warp10client.models._
import com.clevercloud.warp10client.models.gts_module._

implicit val executionContext = system.dispatchers.lookup("yourContext")
implicit val actorMaterializer = ActorMaterializer()
implicit val warpConfiguration = WarpConfiguration("http://www.clever-cloud.com")
val warpClient = WarpClient("clever-cloud.com", "80")

val labels = Map(
    "exactLabel=" -> "label1",
    "regexLabel~" -> "lab.*"
)

warpClient.fetch(
    "READ_TOKEN",
    Query(
        Selector("warpClass", labels),
        FetchRange(LocalDateTime.nowminusSeconds(100), LocalDateTime.now)
    )
).map { gtsSeq =>
    println(Json.toJson(gtsSeq).toString) // using play-json writers
}
```

Have a look at the test directory for functional tests and code examples