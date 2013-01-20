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

class JacksonSerialReader(parser: JsonParser) extends SerialReader with Reflector {

  import definitions._
  
  val ListType = typeOf[List[Any]]
  val MapType = typeOf[Map[String, Any]]
  val StringType = typeOf[String]

  def read[T]()(implicit ttag: TypeTag[T]): T = {
    parser.nextToken()
    readAny(ttag.tpe).asInstanceOf[T]
  }

  def close() { parser.close() }

  private def readAny(tpe: Type): Any = tpe match {
    case x if x <:< BooleanTpe => parser.getBooleanValue()
    case x if x <:< ByteTpe => parser.getByteValue()
    case x if x <:< ShortTpe => parser.getShortValue()
    case x if x <:< IntTpe => parser.getIntValue()
    case x if x <:< LongTpe => parser.getLongValue()
    case x if x <:< FloatTpe => parser.getFloatValue()
    case x if x <:< DoubleTpe => parser.getDoubleValue()
    case x if x <:< CharTpe => parser.getText().head
    case x if x <:< StringType => parser.getText()
    case x if x <:< ListType => readArray(typeArguments(tpe).head)
    case x if x <:< MapType => readMap(typeArguments(tpe)(1))
    case _ => readObject(tpe)
  }

  private def readArray(itemType: Type): List[Any] = {
    if (parser.getCurrentToken() != JsonToken.START_ARRAY)
      throw new JsonParseException("not START_ARRAY", parser.getCurrentLocation())
    val builder = List.newBuilder[Any]
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      builder += readAny(itemType)
    }
    builder.result
  }

  private def readMap(itemType: Type): Map[String, Any] = {
    val builder = Map.newBuilder[String, Any]
    if (parser.getCurrentToken() != JsonToken.START_OBJECT)
      throw new JsonParseException("not START_OBJECT but "+ parser.getCurrentToken(), parser.getCurrentLocation())
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      if (parser.getCurrentToken() != JsonToken.FIELD_NAME)
        throw new JsonParseException("not FIELD_NAME", parser.getCurrentLocation())
      val name = parser.getText()
      parser.nextToken()
      val value = readAny(itemType)
      builder += name -> value
    }
    builder.result
  }

  private def readObject(tpe: Type) = {
    val fields = constructorArguments(tpe)
    val fieldMap = fields.toMap
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
      val fieldType = fieldMap(name)
      val value = readAny(fieldType)
      valueBuilder += name -> value
    }
    val valueMap = valueBuilder.result
    
    val ctor = tpe.declaration(nme.CONSTRUCTOR).asMethod
    val values = ctor.paramss.head map { sym => valueMap(sym.name.decoded) }
    
    val classMirror = currentMirror.reflectClass(tpe.typeSymbol.asClass)
    classMirror.reflectConstructor(ctor).apply(values: _*)
  }

}
