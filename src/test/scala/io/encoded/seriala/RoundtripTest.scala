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
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream

case class Person(name: String, age: Int, children: List[Person])

@RunWith(classOf[JUnitRunner])
class RoundtripTest extends FunSuite with ShouldMatchers {

  test("to JSON and back") {
    val joe = Person("Joe", 107, List(Person("Sue", 78, Nil), Person("Jack", 81, Nil)))
    val out = new ByteArrayOutputStream
    val writer = Seriala.newJsonWriter(out)
    writer.write(joe)
    writer.close()
    
    val json = out.toString("UTF-8")
    println(json)

    val reader = Seriala.newJsonReader(new ByteArrayInputStream(json.getBytes("UTF-8")))
    val joe2 = reader.read[Person]()
    reader.close()
    
    joe2 should equal(joe)
  }

}
