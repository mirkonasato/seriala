//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala.jackson

import io.encoded.seriala.jackson.JsonFactory.{fromString => fromJson, toString => toJson}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, FunSuite}
import org.scalatest.junit.JUnitRunner

case class CaseClass(s: String, i: Int)

@RunWith(classOf[JUnitRunner])
class JsonRoundtripTest extends FunSuite with Matchers {

  test("Boolean") {
    toJson(true) should equal("true")
    fromJson[Boolean]("true") should equal(true)
  }

  test("Int") {
    toJson(7) should equal("7")
    fromJson[Int]("7") should equal(7)
  }

  test("Double") {
    toJson(7.0) should equal("7.0")
    fromJson[Double]("7.0") should equal(7.0)
  }

  test("Option[Int]") {
    toJson(Some(7)) should equal("7")
    fromJson[Option[Int]]("7") should equal(Some(7))
    
    toJson[Option[Int]](None) should equal("null")
    fromJson[Option[Int]]("null") should equal(None)
  }

  test("Seq[Int]") {
    toJson(Seq(1, 2, 3)) should equal("[1,2,3]")
    fromJson[Seq[Int]]("[1,2,3]") should equal(Seq(1, 2, 3))
  }

  test("Map[String,Int]") {
    toJson(Map("a" -> 1, "b" -> 2, "c" -> 3)) should equal("""{"a":1,"b":2,"c":3}""")
    fromJson[Map[String, Int]]("""{"a":1,"b":2,"c":3}""") should equal(Map("a" -> 1, "b" -> 2, "c" -> 3))
  }

  test("CaseClass") {
    val x1 = CaseClass("ABC", 123)
    
    val json = toJson(x1)
    json should equal("""{"s":"ABC","i":123}""")

    val x2 = fromJson[CaseClass](json)
    x2 should equal(x1)
  }

}
