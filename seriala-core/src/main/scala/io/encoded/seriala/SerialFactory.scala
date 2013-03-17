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

  def newSerialWriter[T](out: OutputStream)(implicit typeTag: TypeTag[T]): SerialWriter[T]

  def newSerialReader[T](in: InputStream)(implicit typeTag: TypeTag[T]): SerialReader[T]

  def readSingle[T](in: InputStream)(implicit typeTag: TypeTag[T]): T = {
    val reader = newSerialReader[T](in)
    try
      reader.read()
    finally
      reader.close()
  }

  def fromByteArray[T](bytes: Array[Byte])(implicit typeTag: TypeTag[T]): T =
    readSingle(new ByteArrayInputStream(bytes))

  def fromString[T](value: String)(implicit typeTag: TypeTag[T]): T =
    fromByteArray(value.getBytes("UTF-8"))

  def writeSingle[T](out: OutputStream, x: T)(implicit ttag: TypeTag[T]) {
    val writer = newSerialWriter[T](out)
    try
      writer.write(x)
    finally
      writer.close()
  }

  def toByteArray[T](x: T)(implicit ttag: TypeTag[T]) = {
    val out = new ByteArrayOutputStream
    writeSingle(out, x)
    out.toByteArray()
  }

  def toString[T](x: T)(implicit ttag: TypeTag[T]) =
    new String(toByteArray(x), "UTF-8")

}
