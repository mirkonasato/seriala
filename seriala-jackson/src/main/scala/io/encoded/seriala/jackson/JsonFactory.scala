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
import scala.reflect.runtime.universe.TypeTag
import io.encoded.seriala.SerialReader
import io.encoded.seriala.SerialWriter
import io.encoded.seriala.SerialFactory

class JsonFactory(ignoreUnknown: Boolean) extends SerialFactory {

  val Jackson = new com.fasterxml.jackson.core.JsonFactory()

  def newSerialWriter[T: TypeTag](out: OutputStream): SerialWriter[T] =
    new JacksonSerialWriter[T](Jackson.createGenerator(out))

  def newSerialReader[T: TypeTag](in: InputStream): SerialReader[T] =
    new JacksonSerialReader[T](Jackson.createParser(in), ignoreUnknown)

}

object JsonFactory extends JsonFactory(false)
