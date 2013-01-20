//
// Copyright 2013 Mirko Nasato
//
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
package io.encoded.seriala

import scala.reflect.runtime.universe._

protected trait Reflector {

  protected def typeArguments(tpe: Type): List[Type] = tpe match {
    // see http://stackoverflow.com/questions/12842729/finding-type-parameters-via-reflection-in-scala-2-10
    case t: TypeRefApi => t.args
  }

  protected def constructorArguments(tpe: Type) = {
    val ctor = tpe.member(nme.CONSTRUCTOR)
    if (ctor == NoSymbol) throw new IllegalArgumentException("unsupported type with no constructor: "+ tpe)
    ctor.asMethod.paramss.head map {
      p => (p.name.decoded, p.typeSignature)
    }
  }

}
