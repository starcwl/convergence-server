package com.convergencelabs.server.datastore

import com.convergencelabs.server.domain.DomainFqn

case class TokenPublicKey(id: String, name: String, description: String, keyDate: Long, key: String, enabled: Boolean)
case class TokenKeyPair(publicKey: String, privateKey: String)


case class DomainConfig(
  id: String,
  domainFqn: DomainFqn,
  displayName: String,
  dbUsername: String, 
  dbPassword: String,
  keys: Map[String, TokenPublicKey],
  adminKeyPair: TokenKeyPair)