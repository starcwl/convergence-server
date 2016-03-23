package com.convergencelabs.server.datastore.domain.mapper

import java.util.{ List => JavaList }
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.language.implicitConversions
import com.orientechnologies.orient.core.record.impl.ODocument
import com.convergencelabs.server.util.JValueMapper
import com.convergencelabs.server.datastore.mapper.ODocumentMapper
import com.convergencelabs.server.domain.model.data.ArrayValue
import DataValueMapper.DataValueToODocument
import DataValueMapper.ODocumentToDataValue

object ArrayValueMapper extends ODocumentMapper {

  private[domain] implicit class ArrayValueToODocument(val obj: ArrayValue) extends AnyVal {
    def asODocument: ODocument = arrayValueToODocument(obj)
  }

  private[domain] implicit def arrayValueToODocument(obj: ArrayValue): ODocument = {
    val ArrayValue(vid, children) = obj
    val doc = new ODocument(DocumentClassName)
    doc.field(Fields.VID, vid)
    val docChildren = children map{v => v.asODocument}
    doc.field(Fields.Children, docChildren)
    doc
  }

  private[domain] implicit class ODocumentToArrayValue(val d: ODocument) extends AnyVal {
    def asArrayValue: ArrayValue = oDocumentToArrayValue(d)
  }

  private[domain] implicit def oDocumentToArrayValue(doc: ODocument): ArrayValue = {
    validateDocumentClass(doc, DocumentClassName)

    val vid = doc.field(Fields.VID).asInstanceOf[String]
    ArrayValue(vid, ???)
  }

  private[domain] val DocumentClassName = "ArrayValue"

  private[domain] object Fields {
    val VID = "vid"
    val Children = "children"
  }
}
