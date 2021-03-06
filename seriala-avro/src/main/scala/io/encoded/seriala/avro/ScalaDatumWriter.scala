//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala.avro

import io.encoded.seriala.schema._
import org.apache.avro.{Schema => AvroSchema}
import org.apache.avro.io.DatumWriter
import org.apache.avro.io.Encoder
import scala.reflect.runtime.universe.TypeTag

class ScalaDatumWriter[T: TypeTag] extends DatumWriter[T] {

  val schema = schemaOf[T]

  override def setSchema(avroSchema: AvroSchema) {
    if (avroSchema != SchemaConversions.toAvroSchema(schema))
      throw new IllegalArgumentException("supplied Avro schema incompatible with type "+ schema)
  }

  override def write(datum: T, encoder: Encoder) {
    writeAny(encoder, datum, schema)
  }

  private def writeAny(encoder: Encoder, value: Any, schema: Schema) {
    schema match {
      case BooleanSchema => encoder.writeBoolean(value.asInstanceOf[Boolean])
      case IntSchema => encoder.writeInt(value.asInstanceOf[Int])
      case LongSchema => encoder.writeLong(value.asInstanceOf[Long])
      case FloatSchema => encoder.writeFloat(value.asInstanceOf[Float])
      case DoubleSchema => encoder.writeDouble(value.asInstanceOf[Double])
      case StringSchema => encoder.writeString(value.asInstanceOf[String])
      case s: OptionSchema => writeOption(encoder, value.asInstanceOf[Option[Any]], s.valueSchema)
      case s: MapSchema => writeMap(encoder, value.asInstanceOf[Map[String,Any]], s.valueSchema)
      case s: SeqSchema => writeList(encoder, value.asInstanceOf[List[Any]], s.valueSchema)
      case s: ObjectSchema => writeObject(encoder, value, s)
    }
  }

  private def writeOption(encoder: Encoder, option: Option[Any], valueSchema: Schema) {
    option match {
      case Some(value) =>
        encoder.writeIndex(0)
        writeAny(encoder, value, valueSchema)
      case None =>
        encoder.writeIndex(1)
        encoder.writeNull()
    }
  }

  private def writeList(encoder: Encoder, xs: List[Any], valueSchema: Schema) {
    encoder.writeArrayStart()
    encoder.setItemCount(xs.size)
    for (x <- xs) {
      encoder.startItem()
      writeAny(encoder, x, valueSchema)
    }
    encoder.writeArrayEnd()
  }

  private def writeMap(encoder: Encoder, xs: Map[String, Any], valueSchema: Schema) {
    encoder.writeMapStart()
    encoder.setItemCount(xs.size)
    for ((name, value) <- xs) {
      encoder.startItem()
      encoder.writeString(name)
      writeAny(encoder, value, valueSchema)
    }
    encoder.writeMapEnd()
  }

  private def writeObject(encoder: Encoder, obj: Any, objectSchema: ObjectSchema) {
    for ((fieldName, fieldSchema) <- objectSchema.fields) {
      val fieldValue = objectSchema.getFieldValue(obj, fieldName)
      writeAny(encoder, fieldValue, fieldSchema)
    }
  }

}
