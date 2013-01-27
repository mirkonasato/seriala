//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala

import java.io.InputStream
import java.io.OutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import com.fasterxml.jackson.core.JsonFactory
import scala.reflect.runtime.universe.TypeTag

object Seriala {

  val Jackson = new JsonFactory()

  def newJsonWriter(out: OutputStream): SerialWriter = {
    val generator = Jackson.createGenerator(out)
    new JacksonSerialWriter(generator)
  } 

  def newJsonReader(in: InputStream): SerialReader =
    new JacksonSerialReader(Jackson.createParser(in))

  def newAvroWriter(out: OutputStream): SerialWriter =
    new AvroSerialWriter(out)

  def newAvroReader(in: InputStream): SerialReader =
    new AvroSerialReader(in)

  def fromJson[T](json: String)(implicit ttag: TypeTag[T]): T = {
    val reader = newJsonReader(new ByteArrayInputStream(json.getBytes("UTF-8")))
    try
      reader.read[T]
    finally
      reader.close()
  }

  def toJson[T](x: T)(implicit ttag: TypeTag[T]): String = {
    val out = new ByteArrayOutputStream
    val writer = Seriala.newJsonWriter(out)
    try
      writer.write(x)
    finally
      writer.close()
    out.toString("UTF-8")
  }

}
