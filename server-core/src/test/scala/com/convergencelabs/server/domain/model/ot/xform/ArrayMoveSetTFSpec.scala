package com.convergencelabs.server.domain.model.ot

import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.json4s.JsonAST.JArray
import org.json4s.JsonAST.JString

class ArrayMoveSetTFSpec extends WordSpec with Matchers {

  val Path = List(1, 2)

  "A ArrayMoveSetTF" when {
    "tranforming an array set against an array move" must {

      "noOp the server's move operation and not transform the client's set operation" in {
        val s = ArrayMoveOperation(Path, false, 2, 3)
        val c = ArraySetOperation(Path, false, JArray(List()))

        val (s1, c1) = ArrayMoveSetTF.transform(s, c)

        s1 shouldBe ArrayMoveOperation(Path, true, 2, 3)
        c1 shouldBe c
      }
    }
  }
}
