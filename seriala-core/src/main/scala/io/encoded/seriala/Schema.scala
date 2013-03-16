//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala

import scala.reflect.runtime.universe._

sealed abstract class Schema(val name: String) {
  override def toString = name
}

object BooleanSchema extends Schema("Boolean")
object IntSchema extends Schema("Int")
object LongSchema extends Schema("Long")
object FloatSchema extends Schema("Float")
object DoubleSchema extends Schema("Double")
object StringSchema extends Schema("String")
case class OptionSchema(valueSchema: Schema) extends Schema("Option")
case class ListSchema(valueSchema: Schema) extends Schema("List")
case class MapSchema(valueSchema: Schema) extends Schema("Map")

class ObjectSchema(name: String, val scalaType: Type) extends Schema(name) {

  var _fields: List[(String, Schema)] = null

  def fields: List[(String, Schema)] = _fields

  override def toString = name + "(" + (fields.foldLeft("") { (s, f) => s + ", " + f._1 + ": " + repr(f._2) }) + ")"

  private def repr(schema: Schema) = schema match {
    case x: ObjectSchema => x.name + "(" + (x.fields.foldLeft("") { (s, f) => s + ", " + f._1 + ": " + f._2.name }) + ")"
    case x => x.toString
  }
}

object Schema {

  val BooleanType = typeOf[Boolean]
  val IntType = typeOf[Int]
  val LongType = typeOf[Long]
  val FloatType = typeOf[Float]
  val DoubleType = typeOf[Double]
  val StringType = typeOf[String]
  val OptionType = typeOf[Option[Any]]
  val ListType = typeOf[List[Any]]
  val MapType = typeOf[Map[String, Any]]

  def schemaOf[T]()(implicit typeTag: TypeTag[T]) = buildSchema(typeTag.tpe, Map())

  private def buildSchema(scalaType: Type, knownObjects: Map[Type, Schema]): Schema = scalaType match {
    case t if t <:< BooleanType => BooleanSchema
    case t if t <:< IntType => IntSchema
    case t if t <:< LongType => LongSchema
    case t if t <:< FloatType => FloatSchema
    case t if t <:< DoubleType => DoubleSchema
    case t if t <:< StringType => StringSchema
    case t if t <:< OptionType => new OptionSchema(buildSchema(typeArgs(t)(0), knownObjects))
    case t if t <:< ListType => new ListSchema(buildSchema(typeArgs(t)(0), knownObjects))
    case t if t <:< MapType => new MapSchema(buildSchema(typeArgs(t)(1), knownObjects))
    case t if knownObjects.contains(t) => knownObjects(t)
    case t => {
      // initialize fields later to handle circular dependencies
      val objectSchema = new ObjectSchema(t.typeSymbol.name.decoded, scalaType)
      objectSchema._fields = fieldsOf(t, knownObjects + (t -> objectSchema))
      objectSchema
    }
  }

  private def typeArgs(scalaType: Type): List[Type] = scalaType match {
    // see http://stackoverflow.com/questions/12842729/finding-type-parameters-via-reflection-in-scala-2-10
    case t: TypeRefApi => t.args
  }

  private def fieldsOf(scalaType: Type, knownObjects: Map[Type, Schema]) = {
    val ctor = scalaType.member(nme.CONSTRUCTOR)
    if (ctor == NoSymbol)
      throw new IllegalArgumentException("unsupported type with no constructor: "+ scalaType)
    ctor.asMethod.paramss.head map {
      p => (p.name.decoded, buildSchema(p.typeSignature, knownObjects))
    }
  }

}
