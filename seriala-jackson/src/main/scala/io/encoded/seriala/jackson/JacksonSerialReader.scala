//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala.jackson

import com.fasterxml.jackson.core.{JsonParseException, JsonParser, JsonToken}
import io.encoded.seriala._

import scala.reflect.runtime.universe._

  class JacksonSerialReader[T: TypeTag](parser: JsonParser, ignoreUnkwnown: Boolean = false) extends SerialReader[T] {

  def read(): T = {
    parser.nextToken()
    readAny(Schema.schemaOf[T]).asInstanceOf[T]
  }

  def close() { parser.close() }

  private def readAny(schema: Schema): Any = schema match {
    case BooleanSchema => parser.getBooleanValue
    case IntSchema => parser.getIntValue
    case LongSchema => parser.getLongValue
    case FloatSchema => parser.getFloatValue
    case DoubleSchema => parser.getDoubleValue
    case StringSchema => parser.getText
    case s: OptionSchema => readOption(s.valueSchema)
    case s: MapSchema => readMap(s.valueSchema)
    case s: ListSchema => readList(s.valueSchema)
    case s: ObjectSchema => readObject(s)
  }

  private def readOption(valueSchema: Schema): Option[Any] = {
    if (parser.getCurrentToken == JsonToken.VALUE_NULL) None
    else Some(readAny(valueSchema))
  }

  private def readList(valueSchema: Schema): List[Any] = {
    if (parser.getCurrentToken != JsonToken.START_ARRAY)
      throw new JsonParseException("not START_ARRAY", parser.getCurrentLocation)
    val builder = List.newBuilder[Any]
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      builder += readAny(valueSchema)
    }
    builder.result()
  }

  private def readMap(valueSchema: Schema): Map[String, Any] = {
    val builder = Map.newBuilder[String, Any]
    if (parser.getCurrentToken != JsonToken.START_OBJECT)
      throw new JsonParseException("not START_OBJECT but "+ parser.getCurrentToken, parser.getCurrentLocation)
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      if (parser.getCurrentToken != JsonToken.FIELD_NAME)
        throw new JsonParseException("not FIELD_NAME", parser.getCurrentLocation)
      val name = parser.getText
      parser.nextToken()
      val value = readAny(valueSchema)
      builder += name -> value
    }
    builder.result()
  }

  private def readObject(objectSchema: ObjectSchema) = {
    val fieldMap = objectSchema.fields.toMap
    val valueBuilder = Map.newBuilder[String,Any]

    if (parser.getCurrentToken != JsonToken.START_OBJECT)
      throw new JsonParseException("not START_OBJECT but "+ parser.getCurrentToken, parser.getCurrentLocation)

    while (parser.nextToken() != JsonToken.END_OBJECT) {
      if (parser.getCurrentToken != JsonToken.FIELD_NAME)
        throw new JsonParseException("not FIELD_NAME", parser.getCurrentLocation)
      val name = parser.getText
      parser.nextToken()
      if (fieldMap.contains(name)) {
        val fieldSchema = fieldMap(name)
        val value = readAny(fieldSchema)
        valueBuilder += name -> value        
      } else {
        if (ignoreUnkwnown)
          parser.skipChildren()
        else
          throw new JsonParseException("unknown field: "+ name +" for object "+ objectSchema, parser.getCurrentLocation)
      }
    }
    val valueMap = valueBuilder.result()

    val values: Seq[Any] = objectSchema.fields map {
      case (fieldName, fieldSchema) =>
        if (valueMap.contains(fieldName)) valueMap(fieldName)
        else if (fieldSchema.isInstanceOf[OptionSchema]) None
        else throw new JsonParseException("missing required field "+ fieldName +" for object "+ objectSchema, parser.getCurrentLocation)
    }

    objectSchema.newInstance(values)
  }

}
