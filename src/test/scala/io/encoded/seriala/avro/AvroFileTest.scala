package io.encoded.seriala.avro

import java.io.File
import org.apache.avro.file.DataFileReader
import org.apache.avro.file.DataFileWriter
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import io.encoded.seriala.Schema
import org.scalatest.junit.JUnitRunner

case class Record(name: String, value: Int)

@RunWith(classOf[JUnitRunner])
class AvroFileTest extends FunSuite with ShouldMatchers {

  val avroSchema = SchemaConversions.toAvroSchema(Schema.schemaOf[Record])

  test("Write and read Avro file") {
    val file = File.createTempFile("test", ".avro")
    file.deleteOnExit()
    
    val datumWriter = new ScalaDatumWriter[Record]
    val fileWriter = new DataFileWriter(datumWriter)
    fileWriter.create(avroSchema, file)
    fileWriter.append(Record("one", 1))
    fileWriter.append(Record("two", 2))
    fileWriter.close()
    
    val datumReader = new ScalaDatumReader[Record]
    val fileReader = new DataFileReader(file, datumReader)
    fileReader.next() should equal(Record("one", 1))
    fileReader.next() should equal(Record("two", 2))
    fileReader.hasNext() should equal(false)
  }

}
