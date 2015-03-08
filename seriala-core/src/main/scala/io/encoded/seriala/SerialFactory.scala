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
import scala.reflect.runtime.universe.TypeTag

trait SerialFactory {

  def newSerialWriter[T: TypeTag](out: OutputStream): SerialWriter[T]

  def newSerialReader[T: TypeTag](in: InputStream): SerialReader[T]

  def readSingle[T: TypeTag](in: InputStream): T = {
    val reader = newSerialReader[T](in)
    try
      reader.read()
    finally
      reader.close()
  }

  def fromByteArray[T: TypeTag](bytes: Array[Byte]): T =
    readSingle(new ByteArrayInputStream(bytes))

  def fromString[T: TypeTag](value: String): T =
    fromByteArray(value.getBytes("UTF-8"))

  def writeSingle[T: TypeTag](out: OutputStream, x: T) {
    val writer = newSerialWriter[T](out)
    try
      writer.write(x)
    finally
      writer.close()
  }

  def toByteArray[T: TypeTag](x: T) = {
    val out = new ByteArrayOutputStream
    writeSingle(out, x)
    out.toByteArray
  }

  def toString[T: TypeTag](x: T) =
    new String(toByteArray(x), "UTF-8")

}
