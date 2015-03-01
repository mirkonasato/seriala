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
import io.encoded.seriala.BooleanSchema
import io.encoded.seriala.DoubleSchema
import io.encoded.seriala.FloatSchema
import io.encoded.seriala.IntSchema
import io.encoded.seriala.ListSchema
import io.encoded.seriala.LongSchema
import io.encoded.seriala.MapSchema
import io.encoded.seriala.ObjectSchema
import io.encoded.seriala.OptionSchema
import io.encoded.seriala.Schema
import io.encoded.seriala.SerialReader
import io.encoded.seriala.StringSchema

class AvroSerialReader[T](in: InputStream)(implicit typeTag: TypeTag[T]) extends SerialReader[T] {

  val decoder = DecoderFactory.get().binaryDecoder(in, null)
  val datumReader = new ScalaDatumReader[T]

  def read(): T = datumReader.read(null.asInstanceOf[T], decoder)

  def close() { in.close() }

}
