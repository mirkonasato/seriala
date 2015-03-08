//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala.avro

import scala.reflect.runtime.universe._
import java.io.OutputStream
import org.apache.avro.io.EncoderFactory
import io.encoded.seriala.SerialWriter

class AvroSerialWriter[T: TypeTag](out: OutputStream) extends SerialWriter[T] {

  val encoder = EncoderFactory.get().binaryEncoder(out, null)
  val datumWriter = new ScalaDatumWriter[T]

  def write(x: T) { datumWriter.write(x, encoder) }

  def close() { encoder.flush(); out.close() }

}
