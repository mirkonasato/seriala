//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala

import scala.reflect.runtime.universe._

trait SerialReader[T] {

  def read(): T

  def close()

}