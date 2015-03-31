package io.encoded.seriala

import io.encoded.seriala.schema._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}

import scala.collection.immutable.TreeMap

case class SimpleCaseClass(name: String, value: Int)
case class ComplexCaseClass(name: String, value: Option[SimpleCaseClass], children: Seq[ComplexCaseClass])

@RunWith(classOf[JUnitRunner])
class SchemaTest extends FunSuite with Matchers {

  test("schemaOf simple types") {
    schemaOf[Boolean] should be(BooleanSchema)
    schemaOf[Int] should be(IntSchema)
    schemaOf[Long] should be(LongSchema)
    schemaOf[Float] should be(FloatSchema)
    schemaOf[Double] should be(DoubleSchema)
    schemaOf[String] should be(StringSchema)
  }

  test("schemaOf Option") {
    schemaOf[Option[Int]] should be(OptionSchema(IntSchema))
    schemaOf[Option[String]] should be(OptionSchema(StringSchema))
  }

  test("schemaOf Seq") {
    schemaOf[Seq[Int]] should be(SeqSchema(IntSchema))
    schemaOf[Seq[String]] should be(SeqSchema(StringSchema))
    schemaOf[List[Int]] should be(SeqSchema(IntSchema))
    schemaOf[Vector[Int]] should be(SeqSchema(IntSchema))
  }

  test("schemaOf Map") {
    schemaOf[Map[String, Int]] should be(MapSchema(IntSchema))
    schemaOf[Map[String, String]] should be(MapSchema(StringSchema))
    schemaOf[TreeMap[String, Int]] should be(MapSchema(IntSchema))
  }

  test("schemaOf SimpleCaseClass") {
    val objectSchema = schemaOf[SimpleCaseClass].asInstanceOf[ObjectSchema]
    objectSchema.name should be("SimpleCaseClass")
    objectSchema.fullName should be("io.encoded.seriala.SimpleCaseClass")
    objectSchema.fields should be(Seq("name" -> StringSchema, "value" -> IntSchema))
    objectSchema should equal(schemaOf[SimpleCaseClass])
  }

  test("schemaOf ComplexCaseClass") {
    val objectSchema = schemaOf[ComplexCaseClass].asInstanceOf[ObjectSchema]
    objectSchema.name should be("ComplexCaseClass")
    objectSchema.fullName should be("io.encoded.seriala.ComplexCaseClass")
    objectSchema.fields should be(Seq("name" -> StringSchema, "value" -> OptionSchema(schemaOf[SimpleCaseClass]), "children" -> SeqSchema(objectSchema)))
  }

  test("ObjectSchema newInstance") {
    val objectSchema = schemaOf[SimpleCaseClass].asInstanceOf[ObjectSchema]
    val instance = objectSchema.newInstance(Seq("Joe", 42)).asInstanceOf[SimpleCaseClass]
    instance should be(SimpleCaseClass("Joe", 42))
  }

  test("ObjectSchema getFieldValue") {
    val instance = SimpleCaseClass("Joe", 42)
    val objectSchema = schemaOf[SimpleCaseClass].asInstanceOf[ObjectSchema]
    objectSchema.getFieldValue(instance, "name") should be("Joe")
    objectSchema.getFieldValue(instance, "value") should be(42)
  }

}
