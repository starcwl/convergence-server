package com.convergencelabs.convergence.server.backend.services.domain.permissions

import com.convergencelabs.convergence.server.backend.datastore.domain.permissions.{GroupPermissions, UserPermissions, WorldPermission}

case class RemovePermissions(world: Set[WorldPermission],
                             user: Set[UserPermissions],
                             group: Set[GroupPermissions])
