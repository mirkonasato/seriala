//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala.avro

import scala.reflect.runtime.universe._
import java.io.InputStream
import org.apache.avro.io.DecoderFactory
import io.encoded.seriala.SerialReader

class AvroSerialReader[T: TypeTag](in: InputStream) extends SerialReader[T] {

  val decoder = DecoderFactory.get().binaryDecoder(in, null)
  val datumReader = new ScalaDatumReader[T]

  def read(): T = datumReader.read(null.asInstanceOf[T], decoder)

  def close() { in.close() }

}
