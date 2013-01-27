//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala

import scala.reflect.runtime.universe._
import scala.reflect.runtime.currentMirror
import java.io.OutputStream
import org.apache.avro.io.Encoder
import org.apache.avro.io.EncoderFactory

class AvroSerialWriter(out: OutputStream) extends SerialWriter {

  val encoder = EncoderFactory.get().binaryEncoder(out, null);

  def write[T](x: T)(implicit ttag: TypeTag[T]) {
    writeAny(x, Schema.schemaOf[T])
  }

  def close() {
    encoder.flush()
    out.close()
  }

  private def writeAny(value: Any, schema: Schema) {
    schema match {
      case BooleanSchema => encoder.writeBoolean(value.asInstanceOf[Boolean])
      case IntSchema => encoder.writeInt(value.asInstanceOf[Int])
      case LongSchema => encoder.writeLong(value.asInstanceOf[Long])
      case FloatSchema => encoder.writeFloat(value.asInstanceOf[Float])
      case DoubleSchema => encoder.writeDouble(value.asInstanceOf[Double])
      case StringSchema => encoder.writeString(value.asInstanceOf[String])
      case s: OptionSchema => writeOption(value.asInstanceOf[Option[Any]], s.valueSchema)
      case s: MapSchema => writeMap(value.asInstanceOf[Map[String,Any]], s.valueSchema)
      case s: ListSchema => writeList(value.asInstanceOf[List[Any]], s.valueSchema)
      case s: ObjectSchema => writeObject(value, s)
    }
  }

  private def writeOption(option: Option[Any], valueSchema: Schema) {
    option match {
      case Some(value) => {
        encoder.writeIndex(0)
        writeAny(value, valueSchema)
      }
      case None => {
        encoder.writeIndex(1)
        encoder.writeNull()
      }
    }
  }

  private def writeList(xs: List[Any], valueSchema: Schema) {
    encoder.writeArrayStart()
    encoder.setItemCount(xs.size)
    for (x <- xs) {
      encoder.startItem()
      writeAny(x, valueSchema)
    }
    encoder.writeArrayEnd()
  }

  private def writeMap(xs: Map[String, Any], valueSchema: Schema) {
    encoder.writeMapStart()
    encoder.setItemCount(xs.size)
    for ((name, value) <- xs) {
      encoder.startItem()
      encoder.writeString(name)
      writeAny(value, valueSchema)
    }
    encoder.writeMapEnd()
  }

  private def writeObject(obj: Any, objectSchema: ObjectSchema) {
    val instance = currentMirror.reflect(obj)
    for ((fieldName, fieldSchema) <- objectSchema.fields) {
      val accessor = objectSchema.scalaType.member(stringToTermName(fieldName)).asMethod
      val fieldValue = instance.reflectMethod(accessor).apply()
      writeAny(fieldValue, fieldSchema)
    }
  }

}
