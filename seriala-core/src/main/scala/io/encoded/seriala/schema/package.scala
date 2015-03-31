//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala

import scala.reflect.runtime._
import scala.reflect.runtime.universe._

package object schema {

  sealed abstract class Schema

  object BooleanSchema extends Schema { override def toString = "ObjectSchema" }
  object IntSchema extends Schema { override def toString = "IntSchema" }
  object LongSchema extends Schema { override def toString = "LongSchema" }
  object FloatSchema extends Schema { override def toString = "FloatSchema" }
  object DoubleSchema extends Schema { override def toString = "DoubleSchema" }
  object StringSchema extends Schema { override def toString = "StringSchema" }
  case class OptionSchema(valueSchema: Schema) extends Schema
  case class SeqSchema(valueSchema: Schema) extends Schema
  case class MapSchema(valueSchema: Schema) extends Schema

  class ObjectSchema(scalaType: Type) extends Schema {

    val name = scalaType.typeSymbol.name.decodedName.toString
    val fullName = scalaType.typeSymbol.fullName

    private [schema] var _fields: List[(String, Schema)] = null

    def fields: List[(String, Schema)] = _fields

    def getFieldValue(instance: Any, fieldName: String) = {
      val instanceMirror = currentMirror.reflect(instance)
      val accessor = scalaType.member(TermName(fieldName)).asMethod
      instanceMirror.reflectMethod(accessor).apply()
    }

    def newInstance(args: Seq[Any]): Any = {
      val classMirror = currentMirror.reflectClass(scalaType.typeSymbol.asClass)
      val ctor = scalaType.decl(termNames.CONSTRUCTOR).asMethod
      classMirror.reflectConstructor(ctor).apply(args: _*)
    }

    override def hashCode = (fullName :: fields).hashCode()

    override def equals(other: Any): Boolean = {
      if (!other.isInstanceOf[ObjectSchema]) return false
      val that = other.asInstanceOf[ObjectSchema]
      that.fullName == this.fullName && that.fields == this.fields
    }

    override def toString = "ObjectSchema:"+ name
  }

  private val BooleanType = typeOf[Boolean]
  private val IntType = typeOf[Int]
  private val LongType = typeOf[Long]
  private val FloatType = typeOf[Float]
  private val DoubleType = typeOf[Double]
  private val StringType = typeOf[String]
  private val OptionType = typeOf[Option[Any]]
  private val SeqType = typeOf[Seq[Any]]
  private val MapType = typeOf[Map[String, Any]]

  def schemaOf[T]()(implicit typeTag: TypeTag[T]) = buildSchema(typeTag.tpe, Map())

  private def buildSchema(scalaType: Type, knownObjects: Map[Type, Schema]): Schema = scalaType match {
    case t if t <:< BooleanType => BooleanSchema
    case t if t <:< IntType => IntSchema
    case t if t <:< LongType => LongSchema
    case t if t <:< FloatType => FloatSchema
    case t if t <:< DoubleType => DoubleSchema
    case t if t <:< StringType => StringSchema
    case t if t <:< OptionType => OptionSchema(buildSchema(t.typeArgs(0), knownObjects))
    case t if t <:< SeqType => SeqSchema(buildSchema(t.typeArgs(0), knownObjects))
    case t if t <:< MapType => MapSchema(buildSchema(t.typeArgs(1), knownObjects))
    case t if knownObjects.contains(t) => knownObjects(t)
    case t =>
      // initialize fields later to handle circular dependencies
      val objectSchema = new ObjectSchema(scalaType)
      objectSchema._fields = fieldsOf(t, knownObjects + (t -> objectSchema))
      objectSchema
  }

  private def fieldsOf(scalaType: Type, knownObjects: Map[Type, Schema]) = {
    val ctor = scalaType.member(termNames.CONSTRUCTOR)
    if (ctor == NoSymbol)
      throw new IllegalArgumentException("unsupported type with no constructor: "+ scalaType)
    ctor.asMethod.paramLists.head map {
      p => (p.name.decodedName.toString, buildSchema(p.typeSignature, knownObjects))
    }
  }

}
