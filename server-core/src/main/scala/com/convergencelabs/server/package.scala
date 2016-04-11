package com.convergencelabs

import scala.concurrent.duration.FiniteDuration
import com.typesafe.config.Config
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

package object server {
  case class UnknownErrorResponse(details: String)

  case class HeartbeatConfiguration(
    enabled: Boolean,
    pingInterval: FiniteDuration,
    pongTimeout: FiniteDuration)

  case class ProtocolConfiguration(
    handshakeTimeout: FiniteDuration,
    defaultRequestTimeout: FiniteDuration,
    heartbeatConfig: HeartbeatConfiguration)

  object ProtocolConfigUtil {
    def loadConfig(config: Config): ProtocolConfiguration = {
      val protoConfig = config.getConfig("convergence.protocol")
      ProtocolConfiguration(
        Duration.fromNanos(protoConfig.getDuration("handshake-timeout").toNanos()),
        Duration.fromNanos(protoConfig.getDuration("default-request-timeout").toNanos()),
        HeartbeatConfiguration(
          protoConfig.getBoolean("heartbeat.enabled"),
          Duration.fromNanos(protoConfig.getDuration("heartbeat.ping-interval").toNanos()),
          Duration.fromNanos(protoConfig.getDuration("heartbeat.pong-timeout").toNanos())))
    }
  }
}
