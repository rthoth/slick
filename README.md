# My slick extensions

This is my first [Slick](https://scala-slick.org/) extension.

## Reactive Streams

If you need to run a simple Stream of [DBIActions](https://scala-slick.org/doc/3.3.3/api/index.html#slick.dbio.DBIOAction) you can do this:

```scala
import com.github.rthoth.slick._
import org.reactivestreams.Publisher
import slick.jdbc.H2Profile.api._

val db = Database.forURL("...")
val publisher: Publisher[DBIOAction[_, NoStream, Effect]] =  ...

db.run(publisher) // it has invoked implicit com.github.rthoth.slick.publisherToAction(publisher)

// if you need a transaction?

db.run(publisherToAction(publisher).transactionally)
```

Ok, that's it.