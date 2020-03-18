package db.entities

import slick.jdbc.SQLiteProfile.api._
import slick.lifted.{ProvenShape, Tag}

object Method {
  case class Method(id: Int, fullSignature: String, fileName: String)

  class MethodTable(tag: Tag) extends Table[Method](tag, "MethodsDictionary"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def fullSignature = column[String]("fullSignature")
    def fileName = column[String]("fileName")
    override def * : ProvenShape[Method] = (id, fullSignature, fileName).mapTo[Method]
  }
}