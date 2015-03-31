//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala.avro

import io.encoded.seriala.schema._
import org.apache.avro.{Schema => AvroSchema}
import scala.collection.JavaConversions.seqAsJavaList

object SchemaConversions {

  val NullAvroSchema = AvroSchema.create(AvroSchema.Type.NULL)
  val BooleanAvroSchema = AvroSchema.create(AvroSchema.Type.BOOLEAN)
  val IntAvroSchema = AvroSchema.create(AvroSchema.Type.INT)
  val LongAvroSchema = AvroSchema.create(AvroSchema.Type.LONG)
  val FloatAvroSchema = AvroSchema.create(AvroSchema.Type.FLOAT)
  val DoubleAvroSchema =  AvroSchema.create(AvroSchema.Type.DOUBLE)
  val StringDoubleSchema = AvroSchema.create(AvroSchema.Type.STRING)

  def toAvroSchema(schema: Schema): AvroSchema = buildSchema(schema, Map())

  private def buildSchema(schema: Schema, recordMap: Map[Schema, AvroSchema]): AvroSchema = {
    schema match {
      case BooleanSchema => BooleanAvroSchema
      case IntSchema => IntAvroSchema
      case LongSchema => LongAvroSchema
      case FloatSchema => FloatAvroSchema
      case DoubleSchema => DoubleAvroSchema
      case StringSchema => StringDoubleSchema
      case s: OptionSchema => AvroSchema.createUnion(List(buildSchema(s.valueSchema, recordMap), NullAvroSchema))
      case s: MapSchema => AvroSchema.createMap(buildSchema(s.valueSchema, recordMap))
      case s: SeqSchema => AvroSchema.createArray(buildSchema(s.valueSchema, recordMap))
      case s if recordMap.contains(s) => recordMap(s)
      case s: ObjectSchema =>
        val recordSchema = AvroSchema.createRecord(s.name, null, null, false)
        val updatedMap = recordMap + (s -> recordSchema)
        val avroFields = s.fields map { field =>
          val (fieldName, fieldSchema) = field
          new AvroSchema.Field(fieldName, buildSchema(fieldSchema, updatedMap), null, null)
        }
        recordSchema.setFields(avroFields)
        recordSchema
    }
  }

}
