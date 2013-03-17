//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala.avro

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import scala.reflect.runtime.universe._
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import org.scalatest.junit.JUnitRunner
import io.encoded.seriala.avro.AvroFactory.{fromByteArray => fromAvro, toByteArray => toAvro}

case class CaseClass(s: String, i: Int)

@RunWith(classOf[JUnitRunner])
class AvroRoundtripTest extends FunSuite with ShouldMatchers {

  test("Boolean") {
    fromAvro[Boolean](toAvro(true)) should equal(true)
  }

  test("Int") {
    fromAvro[Int](toAvro(7)) should equal(7)
  }

  test("Double") {
    fromAvro[Double](toAvro(7.0)) should equal(7.0)
  }

  test("Option[Int]") {
    fromAvro[Option[Int]](toAvro(Some(7))) should equal(Some(7))
    fromAvro[Option[Int]](toAvro[Option[Int]](None)) should equal(None)
  }

  test("List[Int]") {
    val x = List(1, 2, 3)
    fromAvro[List[Int]](toAvro(x)) should equal(x)
  }

  test("Map[String,Int]") {
    val x = Map("a" -> 1, "b" -> 2, "c" -> 3)
    fromAvro[Map[String, Int]](toAvro(x)) should equal(x)
  }

  test("CaseClass") {
    val x = CaseClass("ABC", 123)
    fromAvro[CaseClass](toAvro(x)) should equal(x)
  }

}
