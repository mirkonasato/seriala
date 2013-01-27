//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala.jackson

import io.encoded.seriala._
import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._
import com.fasterxml.jackson.core.JsonGenerator

class JacksonSerialWriter[T](generator: JsonGenerator)(implicit typeTag: TypeTag[T]) extends SerialWriter[T] {

  def write(x: T) {
    writeAny(x, Schema.schemaOf[T])
  }

  def close() { generator.close() }

  private def writeAny(value: Any, schema: Schema) {
    schema match {
      case BooleanSchema => generator.writeBoolean(value.asInstanceOf[Boolean])
      case IntSchema => generator.writeNumber(value.asInstanceOf[Int])
      case LongSchema => generator.writeNumber(value.asInstanceOf[Long])
      case FloatSchema => generator.writeNumber(value.asInstanceOf[Float])
      case DoubleSchema => generator.writeNumber(value.asInstanceOf[Double])
      case StringSchema => generator.writeString(value.asInstanceOf[String])
      case s: OptionSchema => writeOption(value.asInstanceOf[Option[Any]], s.valueSchema)
      case s: MapSchema => writeMap(value.asInstanceOf[Map[String,Any]], s.valueSchema)
      case s: ListSchema => writeList(value.asInstanceOf[List[Any]], s.valueSchema)
      case s: ObjectSchema => writeObject(value, s)
    }
  }

  private def writeOption(option: Option[Any], valueSchema: Schema) {
    option match {
      case None => generator.writeNull()
      case Some(value) => writeAny(value, valueSchema)
    }
  }

  private def writeList(xs: List[Any], valueSchema: Schema) {
    generator.writeStartArray()
    for (x <- xs) writeAny(x, valueSchema)
    generator.writeEndArray()
  }

  private def writeMap(xs: Map[String, Any], valueSchema: Schema) {
    generator.writeStartObject()
    for ((name, value) <- xs) {
      generator.writeFieldName(name)
      writeAny(value, valueSchema)
    }
    generator.writeEndObject()
  }

  private def writeObject(obj: Any, objectSchema: ObjectSchema) {
    val instance = currentMirror.reflect(obj)
    generator.writeStartObject()
    for ((fieldName, fieldSchema) <- objectSchema.fields) {
      val accessor = objectSchema.scalaType.member(stringToTermName(fieldName)).asMethod
      val fieldValue = instance.reflectMethod(accessor).apply()
      generator.writeFieldName(fieldName)
      writeAny(fieldValue, fieldSchema)
    }
    generator.writeEndObject()
  }

}
