package com.convergencelabs.server.domain.model.ot

import org.json4s.JsonAST.JObject
import org.scalatest.Finders
import org.scalatest.Matchers
import org.scalatest.WordSpec

// scalastyle:off multiple.string.literals
class ObjectSetPropertyRemovePropertyTFSpec extends WordSpec with Matchers {

  "A ObjectSetPropertyRemovePropertyTF" when {

    "tranforming a set and a set operation " must {
      "do not transform the operations if the properties are unequal" in {
        val s = ObjectSetPropertyOperation(List(), false, "prop1", JObject())
        val c = ObjectRemovePropertyOperation(List(), false, "prop2")

        val (s1, c1) = ObjectSetPropertyRemovePropertyTF.transform(s, c)

        s1 shouldBe s
        c1 shouldBe c
      }

      "transform a set into an add if the set and the remove are the same property, noOp the remove" in {
        val s = ObjectSetPropertyOperation(List(), false, "prop", JObject())
        val c = ObjectRemovePropertyOperation(List(), false, "prop")

        val (s1, c1) = ObjectSetPropertyRemovePropertyTF.transform(s, c)

        s1 shouldBe ObjectAddPropertyOperation(List(), false, "prop", JObject())
        c1 shouldBe ObjectRemovePropertyOperation(List(), true, "prop")
      }
    }
  }
}
