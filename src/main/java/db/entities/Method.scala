package db.entities

import slick.jdbc.SQLiteProfile.api._
import slick.lifted.{ProvenShape, Tag}

object Method {
  case class Method(id: Long, fullSignature: String, fileName: String) {
    override def equals(obj: Any): Boolean = {
      obj match {
        case method: Method => method.fullSignature == fullSignature && method.fileName == fileName
        case _ => false
      }
    }
  }

  class MethodTable(tag: Tag) extends Table[Method](tag, "MethodsDictionary"){
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def fullSignature = column[String]("fullSignature")
    def fileName = column[String]("fileName")
    override def * : ProvenShape[Method] = (id, fullSignature, fileName).mapTo[Method]
  }

  object MethodTable {
    def insertMethod(methods: Seq[Method]): String = {
      val methodsList = TableQuery[MethodTable]
      DBIO.seq(methodsList ++= methods)
      methodsList.distinct.insertStatement
    }
  }
}