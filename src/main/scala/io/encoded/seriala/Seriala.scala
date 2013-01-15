//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala

import java.io.InputStream
import java.io.OutputStream

import com.fasterxml.jackson.core.JsonFactory

object Seriala {

  val Jackson = new JsonFactory()

  def newJsonWriter(out: OutputStream): SerialWriter =
    new JacksonSerialWriter(Jackson.createGenerator(out)) 

  def newJsonReader(in: InputStream): SerialReader =
    new JacksonSerialReader(Jackson.createParser(in))

}
