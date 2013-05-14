//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala.jackson

import io.encoded.seriala.jackson.JsonFactory.{fromString => fromJson, toString => toJson}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import scala.reflect.runtime.universe._
import org.scalatest.junit.JUnitRunner
import com.fasterxml.jackson.core.JsonParseException

case class ClassWithOptionalField(id: String, foreignId: Option[String] = None)

@RunWith(classOf[JUnitRunner])
class JsonSerialReaderTest extends FunSuite with ShouldMatchers {

  test("missing optional field") {
    fromJson[ClassWithOptionalField]("""{"id":"test-id"}""") should equal(ClassWithOptionalField("test-id"))
  }

  test("missing required field") {
    intercept[JsonParseException] {
      fromJson[ClassWithOptionalField]("""{"foreignId":"test-foreign-id"}""")
    }
  }

  test("unknown fields are not accepted by default") {
    intercept[JsonParseException] {
      fromJson[ClassWithOptionalField]("""{"id":"test-id","otherProperty":"other value"}""")
    }
  }

  test("unknown fields can optionally be ignored") {
    val jsonFactory = new JsonFactory(ignoreUnknown = true)
    jsonFactory.fromString[ClassWithOptionalField]("""{"id":"test-id","otherProperty":[1,2,3]}""")
  }

}
