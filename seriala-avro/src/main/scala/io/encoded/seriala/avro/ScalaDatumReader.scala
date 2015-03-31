package io.encoded.seriala.avro

import io.encoded.seriala.schema._
import org.apache.avro.io.DatumReader
import org.apache.avro.io.Decoder
import org.apache.avro.{Schema => AvroSchema}
import scala.reflect.runtime.universe.TypeTag

class ScalaDatumReader[T: TypeTag] extends DatumReader[T] {

  val schema = schemaOf[T]

  override def setSchema(avroSchema: AvroSchema) {
    if (avroSchema != SchemaConversions.toAvroSchema(schema))
      throw new IllegalArgumentException("supplied Avro schema incompatible with type "+ schema)
  }

  override def read(reuse: T, decoder: Decoder): T =
    readAny(decoder, schema).asInstanceOf[T]

  private def readAny(decoder: Decoder, schema: Schema): Any = schema match {
    case BooleanSchema => decoder.readBoolean()
    case IntSchema => decoder.readInt()
    case LongSchema => decoder.readLong()
    case FloatSchema => decoder.readFloat()
    case DoubleSchema => decoder.readDouble()
    case StringSchema => decoder.readString()
    case s: OptionSchema => readOption(decoder, s.valueSchema)
    case s: MapSchema => readMap(decoder, s.valueSchema)
    case s: ListSchema => readList(decoder, s.valueSchema)
    case s: ObjectSchema => readObject(decoder, s)
  }

  private def readOption(decoder: Decoder, valueSchema: Schema): Option[Any] = {
    decoder.readIndex() match {
      case 0 => Some(readAny(decoder, valueSchema))
      case 1 => None
    }
  }

  private def readList(decoder: Decoder, valueSchema: Schema): List[Any] = {
    val builder = List.newBuilder[Any]
    var i = 0L
    var size = decoder.readArrayStart()
    while (i < size) {
      builder += readAny(decoder, valueSchema)
      i += 1
      if (i == size) {
        i = 0
        size = decoder.arrayNext()
      }
    }
    builder.result()
  }

  private def readMap(decoder: Decoder, valueSchema: Schema): Map[String, Any] = {
    val builder = Map.newBuilder[String, Any]
    var i = 0L
    var size = decoder.readMapStart()
    while (i < size) {
      val name = decoder.readString()
      val value = readAny(decoder, valueSchema)
      builder += name -> value
      i += 1
      if (i == size) {
        i = 0
        size = decoder.mapNext()
      }
    }
    builder.result()
  }

  private def readObject(decoder: Decoder, objectSchema: ObjectSchema) = {
    val values = List.newBuilder[Any]
    for ((_, fieldSchema) <- objectSchema.fields)
      values += readAny(decoder, fieldSchema)
    objectSchema.newInstance(values.result())
  }

}
