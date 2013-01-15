//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala

import com.fasterxml.jackson.core.JsonGenerator
import scala.reflect.runtime.universe._
import scala.reflect.runtime.currentMirror

class JacksonSerialWriter(generator: JsonGenerator) extends SerialWriter with Reflector {

  def write[T](x: T)(implicit ttag: TypeTag[T]) {
    writeAny(x, ttag.tpe)
  }

  def close() { generator.close() }

  private def writeAny(any: Any, tpe: Type) {
    any match {
      case x: Boolean => generator.writeBoolean(x)
      case x: Byte => generator.writeNumber(x)
      case x: Short => generator.writeNumber(x)
      case x: Int => generator.writeNumber(x)
      case x: Long => generator.writeNumber(x)
      case x: Float => generator.writeNumber(x)
      case x: Double => generator.writeNumber(x)
      case x: Char => generator.writeString(x.toString)
      case x: String => generator.writeString(x)
      case x: Seq[_] => writeSeq(x, typeArguments(tpe).head)
      case _ => writeObject(any, tpe)
    }
  }

  private def writeSeq[T](xs: Seq[T], itemType: Type) {
    generator.writeStartArray()
    for (x <- xs)
      writeAny(x, itemType)
    generator.writeEndArray()
  }

  private def writeObject(x: Any, tpe: Type) {
    val instance = currentMirror.reflect(x)
    generator.writeStartObject()
    for ((name, argType) <- constructorArguments(tpe)) {
      val accessor = tpe.member(stringToTermName(name)).asMethod
      val value = instance.reflectMethod(accessor).apply()
      generator.writeFieldName(name)
      writeAny(value, argType)
    }
    generator.writeEndObject()
  }

}
