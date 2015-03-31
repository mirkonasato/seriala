package io.encoded.seriala

import io.encoded.seriala.schema._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}

import scala.collection.immutable.TreeMap

@RunWith(classOf[JUnitRunner])
class SchemaTest extends FunSuite with Matchers {

  test("schemaOf basic types") {
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

}
