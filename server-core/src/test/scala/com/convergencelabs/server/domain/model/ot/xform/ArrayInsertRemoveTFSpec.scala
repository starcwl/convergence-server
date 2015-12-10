package com.convergencelabs.server.domain.model.ot

import org.json4s.JsonAST.JString
import org.scalatest.Finders
import org.scalatest.WordSpec
import org.scalatest.Matchers

// scalastyle:off magic.number
class ArrayInsertRemoveTFSpec
    extends WordSpec
    with Matchers {

  val Path = List(1, 2)
  val ClientVal = JString("x")
  val ServerVal = JString("y")

  "A ArrayInsertRemoveTF" when {

    "tranforming a server insert against a client remove " must {

      "increment the client index if the server's index is before the client's" in {
        val s = ArrayInsertOperation(Path, false, 2, ServerVal)
        val c = ArrayRemoveOperation(Path, false, 3)

        val (s1, c1) = ArrayInsertRemoveTF.transform(s, c)

        s1 shouldBe s
        c1 shouldBe ArrayRemoveOperation(Path, false, 4)
      }

      /**
       * <pre>
       *
       * Indices         : [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
       * Original Array  : [A, B, C, D, E, F, G, H, I, J]
       *
       * Server Op       :        ^                              Insert(2, X)
       * Client Op       :        ^                              Remove(2, C)
       *
       * Server State    : [A, B, X, C, D, E, F, G, H, I, J]
       * Client Op'      :           ^                           Remove(3, C)
       *
       * Client State    : [A, B, D, E, F, G, H, I, J]
       * Server Op'      :        ^                              Insert(2, X)
       *
       * Converged State : [A, B, X, D, E, F, G, H, I, J]
       *
       * </pre>
       */
      "increment the client index if both operations target the same index" in {
        val s = ArrayInsertOperation(Path, false, 2, ServerVal)
        val c = ArrayRemoveOperation(Path, false, 2)

        val (s1, c1) = ArrayInsertRemoveTF.transform(s, c)

        s1 shouldBe s
        c1 shouldBe ArrayRemoveOperation(Path, false, 3)
      }

      "decrement the server index if the server's index is after the client's" in {
        val s = ArrayInsertOperation(Path, false, 3, ServerVal)
        val c = ArrayRemoveOperation(Path, false, 2)

        val (s1, c1) = ArrayInsertRemoveTF.transform(s, c)

        s1 shouldBe ArrayInsertOperation(Path, false, 2, ServerVal)
        c1 shouldBe c
      }
    }
  }
}
