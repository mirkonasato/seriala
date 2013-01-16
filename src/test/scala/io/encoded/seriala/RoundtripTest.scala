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

case class Person(name: String, age: Int, children: List[Person])

@RunWith(classOf[JUnitRunner])
class RoundtripTest extends FunSuite with ShouldMatchers {

  test("to JSON and back") {
    val joe = Person("Joe", 107, List(Person("Sue", 78, Nil), Person("Jack", 81, Nil)))
    
    val json = Seriala.toJson(joe)
    println(json)

    val joe2 = Seriala.fromJson[Person](json)
    joe2 should equal(joe)
  }

}
