//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala.jackson

import java.io.InputStream
import java.io.OutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import com.fasterxml.jackson.core.JsonFactory
import scala.reflect.runtime.universe.TypeTag
import io.encoded.seriala.SerialReader
import io.encoded.seriala.SerialWriter

object JacksonSeriala {

  val Jackson = new JsonFactory()

  def newJsonWriter[T](out: OutputStream)(implicit typeTag: TypeTag[T]): SerialWriter[T] = {
    val generator = Jackson.createGenerator(out)
    new JacksonSerialWriter[T](generator)
  } 

  def newJsonReader[T](in: InputStream)(implicit typeTag: TypeTag[T]): SerialReader[T] =
    new JacksonSerialReader[T](Jackson.createParser(in))

  def fromJson[T](in: InputStream)(implicit typeTag: TypeTag[T]): T = {
    val reader = newJsonReader[T](in)
    try
      reader.read
    finally
      reader.close()
  }

  def fromJson[T](json: String)(implicit ttag: TypeTag[T]): T =
    fromJson[T](new ByteArrayInputStream(json.getBytes("UTF-8")))

  def toJson[T](out: OutputStream, x: T)(implicit ttag: TypeTag[T]) {
    val writer = JacksonSeriala.newJsonWriter[T](out)
    try
      writer.write(x)
    finally
      writer.close()
  }

  def toJson[T](x: T)(implicit ttag: TypeTag[T]): String = {
    val out = new ByteArrayOutputStream
    toJson(out, x)
    out.toString("UTF-8")
  }

}
