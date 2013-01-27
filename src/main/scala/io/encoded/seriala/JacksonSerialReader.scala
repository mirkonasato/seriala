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

class JacksonSerialReader(parser: JsonParser) extends SerialReader {

  def read[T]()(implicit ttag: TypeTag[T]): T = {
    parser.nextToken()
    readAny(Schema.schemaOf[T]).asInstanceOf[T]
  }

  def close() { parser.close() }

  private def readAny(schema: Schema): Any = schema match {
    case BooleanSchema => parser.getBooleanValue()
    case IntSchema => parser.getIntValue()
    case LongSchema => parser.getLongValue()
    case FloatSchema => parser.getFloatValue()
    case DoubleSchema => parser.getDoubleValue()
    case StringSchema => parser.getText()
    case s: OptionSchema => readOption(s.valueSchema)
    case s: MapSchema => readMap(s.valueSchema)
    case s: ListSchema => readList(s.valueSchema)
    case s: ObjectSchema => readObject(s)
  }

  private def readOption(valueSchema: Schema): Option[Any] = {
    if (parser.getCurrentToken() == JsonToken.VALUE_NULL) None
    else Some(readAny(valueSchema))
  }

  private def readList(valueSchema: Schema): List[Any] = {
    if (parser.getCurrentToken() != JsonToken.START_ARRAY)
      throw new JsonParseException("not START_ARRAY", parser.getCurrentLocation())
    val builder = List.newBuilder[Any]
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      builder += readAny(valueSchema)
    }
    builder.result
  }

  private def readMap(valueSchema: Schema): Map[String, Any] = {
    val builder = Map.newBuilder[String, Any]
    if (parser.getCurrentToken() != JsonToken.START_OBJECT)
      throw new JsonParseException("not START_OBJECT but "+ parser.getCurrentToken(), parser.getCurrentLocation())
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      if (parser.getCurrentToken() != JsonToken.FIELD_NAME)
        throw new JsonParseException("not FIELD_NAME", parser.getCurrentLocation())
      val name = parser.getText()
      parser.nextToken()
      val value = readAny(valueSchema)
      builder += name -> value
    }
    builder.result
  }

  private def readObject(objectSchema: ObjectSchema) = {
    val fieldMap = objectSchema.fields.toMap
    val valueBuilder = Map.newBuilder[String,Any]
    
    if (parser.getCurrentToken() != JsonToken.START_OBJECT)
      throw new JsonParseException("not START_OBJECT but "+ parser.getCurrentToken(), parser.getCurrentLocation())
    
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      if (parser.getCurrentToken() != JsonToken.FIELD_NAME)
        throw new JsonParseException("not FIELD_NAME", parser.getCurrentLocation())
      val name = parser.getText()
      if (!fieldMap.contains(name))
        throw new JsonParseException("unknown field: "+ name, parser.getCurrentLocation())
      
      parser.nextToken()
      val fieldSchema = fieldMap(name)
      val value = readAny(fieldSchema)
      valueBuilder += name -> value
    }
    val valueMap = valueBuilder.result
    
    val ctor = objectSchema.scalaType.declaration(nme.CONSTRUCTOR).asMethod
    val values = ctor.paramss.head map { sym => valueMap(sym.name.decoded) }
    
    val classMirror = currentMirror.reflectClass(objectSchema.scalaType.typeSymbol.asClass)
    classMirror.reflectConstructor(ctor).apply(values: _*)
  }

}
