package db.entities

import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

case class MethodChange(dateOfChange: Long, authorName: String, branchName: String, signatureId: Int)

class MethodChangelogTable(tag: Tag) extends Table[MethodChange](tag, "MethodsDictionary"){
  def dateOfChange = column[Long]("dtChanged")
  def authorName = column[String]("authorName")
  def branchName = column[String]("branchName")
  def signatureId = column[Int]("signatureId")
  def methodFK = foreignKey("fk_sig_id", signatureId, MethodTable)
  override def * : ProvenShape[Method] = (id, fullSignature, fileName).mapTo[Method]
}