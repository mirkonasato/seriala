//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala

import com.fasterxml.jackson.core.JsonParser
import scala.reflect.runtime.universe._
import scala.reflect.runtime.currentMirror
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonToken
import java.io.InputStream
import org.apache.avro.io.DecoderFactory

class AvroSerialReader(in: InputStream) extends SerialReader {

  val decoder = DecoderFactory.get().binaryDecoder(in, null);

  def read[T]()(implicit ttag: TypeTag[T]): T = {
    readAny(Schema.schemaOf[T]).asInstanceOf[T]
  }

  def close() {
    in.close()
  }

  private def readAny(schema: Schema): Any = schema match {
    case BooleanSchema => decoder.readBoolean()
    case IntSchema => decoder.readInt()
    case LongSchema => decoder.readLong()
    case FloatSchema => decoder.readFloat()
    case DoubleSchema => decoder.readDouble()
    case StringSchema => decoder.readString()
    case s: OptionSchema => readOption(s.valueSchema)
    case s: MapSchema => readMap(s.valueSchema)
    case s: ListSchema => readList(s.valueSchema)
    case s: ObjectSchema => readObject(s)
  }

  private def readOption(valueSchema: Schema): Option[Any] = {
    decoder.readIndex() match {
      case 0 => Some(readAny(valueSchema))
      case 1 => None
    }
  }

  private def readList(valueSchema: Schema): List[Any] = {
    val builder = List.newBuilder[Any]
    var i = 0L
    var size = decoder.readArrayStart()
    while (i < size) {
      builder += readAny(valueSchema)
      i += 1
      if (i == size) {
        i = 0
        size = decoder.arrayNext()
      }
    }
    builder.result
  }

  private def readMap(valueSchema: Schema): Map[String, Any] = {
    val builder = Map.newBuilder[String, Any]
    var i = 0L
    var size = decoder.readMapStart()
    while (i < size) {
      val name = decoder.readString()
      val value = readAny(valueSchema)
      builder += name -> value
      i += 1
      if (i == size) {
        i = 0
        size = decoder.mapNext()
      }
    }
    builder.result
  }

  private def readObject(objectSchema: ObjectSchema) = {
    val values = List.newBuilder[Any]
    for ((_, fieldSchema) <- objectSchema.fields)
      values += readAny(fieldSchema)
    val ctor = objectSchema.scalaType.declaration(nme.CONSTRUCTOR).asMethod
    val classMirror = currentMirror.reflectClass(objectSchema.scalaType.typeSymbol.asClass)
    classMirror.reflectConstructor(ctor).apply(values.result: _*)
  }

}
