/*
 * Copyright (c) 2019 - Convergence Labs, Inc.
 *
 * This file is part of the Convergence Server, which is released under
 * the terms of the GNU General Public License version 3 (GPLv3). A copy
 * of the GPLv3 should have been provided along with this file, typically
 * located in the "LICENSE" file, which is part of this source code package.
 * Alternatively, see <https://www.gnu.org/licenses/gpl-3.0.html> for the
 * full text of the GPLv3 license, if it was not provided.
 */

package com.convergencelabs.convergence.server.domain.model.ot.xform.array

import com.convergencelabs.convergence.server.domain.model.ot.xform.OperationTransformationFunction
import com.convergencelabs.convergence.server.domain.model.ot._

private[ot] object ArrayRemoveRemoveTF extends OperationTransformationFunction[ArrayRemoveOperation, ArrayRemoveOperation] {
  def transform(s: ArrayRemoveOperation, c: ArrayRemoveOperation): (ArrayRemoveOperation, ArrayRemoveOperation) = {
    if (s.index == c.index) {
      // A-RR-2
      (s.copy(noOp = true), c.copy(noOp = true))
    } else if (s.index < c.index) {
      // A-RR-1
      (s, c.copy(index = c.index - 1))
    } else {
      // A-RR-3
      (s.copy(index = s.index - 1), c)
    }
  }
}
