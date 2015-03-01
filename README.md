Seriala
=======

A serialization library for Scala.

This is in experimental stage, started as a way to try out the then new Scala 2.10
[reflection API](http://docs.scala-lang.org/overviews/reflection/overview.html), and now updated for Scala 2.11.

Design Goals
------------

* Serializable objects should be regular Scala classes (no annotations, no base trait)
* Support multiple formats; currently JSON (using Jackson) and Avro
* For schema-based formats, support generating the schema from Scala classes, or vice-versa

JSON Example
------------

    scala> case class User(id: Int, name: String, groups: List[String])
    defined class User
    
    scala> val joe = User(1000, "joe", List("admin", "staff"))
    joe: User = User(1000,joe,List(admin, staff))
    
    scala> val json = JsonFactory.toString(joe)
    json: String = {"id":1000,"name":"joe","groups":["admin","staff"]}
    
    scala> val joe2 = JsonFactory.fromString[User](json)
    joe2: User = User(1000,joe,List(admin, staff))

Avro Example
------------

DatumWriter and DatumReader implementations are provided for dealing with
[Avro files](http://avro.apache.org/docs/1.7.7/gettingstartedjava.html#Serializing):

    val avroSchema = SchemaConversions.toAvroSchema(Schema.schemaOf[User])

    val datumWriter = new ScalaDatumWriter[User]
    val fileWriter = new DataFileWriter(datumWriter)
    fileWriter.create(avroSchema, file)
    fileWriter.append(user)
    // ...
    fileWriter.close()

    val datumReader = new ScalaDatumReader[User]
    val fileReader = new DataFileReader(file, datumReader)
    while (fileReader.hasNext()) {
      val user = fileReader.next()
    }
