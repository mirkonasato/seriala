//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import io.encoded.seriala.Seriala._

case class CaseClass(s: String, i: Int)

@RunWith(classOf[JUnitRunner])
class RoundtripTest extends FunSuite with ShouldMatchers {

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

  test("List[Int]") {
    toJson(List(1, 2, 3)) should equal("[1,2,3]")
    fromJson[List[Int]]("[1,2,3]") should equal(List(1, 2, 3))
  }

  test("Map[String,Int]") {
    toJson(Map("a" -> 1, "b" -> 2, "c" -> 3)) should equal("{\"a\":1,\"b\":2,\"c\":3}")
    fromJson[Map[String, Int]]("{\"a\":1,\"b\":2,\"c\":3}") should equal(Map("a" -> 1, "b" -> 2, "c" -> 3))
  }

  test("Char") {
    toJson('A') should equal("\"A\"")
    fromJson[Char]("\"A\"") should equal('A')
  }

  test("CaseClass") {
    val x1 = CaseClass("ABC", 123)
    
    val json = toJson(x1)
    json should equal("""{"s":"ABC","i":123}""")

    val x2 = fromJson[CaseClass](json)
    x2 should equal(x1)
  }

}
