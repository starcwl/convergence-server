package com.convergencelabs.server.testkit

import java.io.File

import com.convergencelabs.server.ConvergenceServerNode
import com.orientechnologies.common.log.OLogManager
import com.orientechnologies.orient.core.command.OCommandOutputListener
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import com.orientechnologies.orient.core.db.tool.ODatabaseImport
import com.typesafe.config.ConfigFactory

import grizzled.slf4j.Logging

object TestServer {
  def main(args: Array[String]): Unit = {
    val server = new TestServer(
      "test-server/mono-server-application.conf",
      Map(
        "convergence" -> "test-server/convergence.json.gz",
        "namespace1-domain1" -> "test-server/n1-d1.json.gz"))
    server.start()
  }
}

class TestServer(
  configFile: String,
  databases: Map[String, String])
    extends Logging {

  var openedDatabases = List[ODatabaseDocumentTx]()

  // Override the configuration of the port
  val config = ConfigFactory.parseFile(new File("test-server/convergence-application.conf"))
  val server = new ConvergenceServerNode(config)

  def start(): Unit = {
    logger.info("Test Server starting up")
    OLogManager.instance().setConsoleLevel("WARNING")

    // Set Up OrientDB database
    databases.foreach { case (id, file) => importDatabase(id, file) }

    server.start()

    logger.info("Test Server started.")
  }

  def stop(): Unit = {
    server.stop()

    openedDatabases.foreach { db =>
      db.drop()
    }
  }

  def importDatabase(dbName: String, importFile: String): Unit = {
    val db = new ODatabaseDocumentTx(s"memory:$dbName")
    db.activateOnCurrentThread()
    db.create()

    val dbImport = new ODatabaseImport(db, importFile, new OCommandOutputListener() { def onMessage(message: String) {} })
    dbImport.importDatabase()
    dbImport.close()

    db.getMetadata.reload()

    openedDatabases = openedDatabases :+ db
  }
}
