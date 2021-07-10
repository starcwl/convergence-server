/*
 * Copyright (c) 2021 - Convergence Labs, Inc.
 *
 * This file is part of the Convergence Server, which is released under
 * the terms of the GNU General Public License version 3 (GPLv3). A copy
 * of the GPLv3 should have been provided along with this file, typically
 * located in the "LICENSE" file, which is part of this source code package.
 * Alternatively, see <https://www.gnu.org/licenses/gpl-3.0.html> for the
 * full text of the GPLv3 license, if it was not provided.
 */

package com.convergencelabs.convergence.server.backend.services.domain.model.ot.xform.string

import com.convergencelabs.convergence.server.backend.services.domain.model.ot._
import com.convergencelabs.convergence.server.backend.services.domain.model.ot.xform.MockModel

class MockStringModel(private var state: String) extends MockModel {

  def updateModel(op: DiscreteOperation): Unit = {
    op match {
      case splice: StringSpliceOperation => handleSplice(splice)
      case set: StringSetOperation => handleSet(set)
      case x: Any => throw new IllegalArgumentException()
    }
  }

  private def handleSplice(op: StringSpliceOperation): Unit = {
    state = state.patch(op.index, op.insertValue, op.deleteCount)
  }

  private def handleSet(op: StringSetOperation): Unit = {
    state = op.value
  }

  def getData(): String = {
    state
  }
}
