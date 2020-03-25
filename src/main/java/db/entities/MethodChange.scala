package db.entities

import db.entities.Method.MethodTable
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.{ProvenShape, Tag}

case class MethodChange(dateOfChange: Long, authorName: String, branchName: String, signatureId: Int)

class MethodChangelogTable(tag: Tag) extends Table[MethodChange](tag, "MethodsDictionary"){
  def dateOfChange = column[Long]("dtChanged")
  def authorName = column[String]("authorName")
  def branchName = column[String]("branchName")
  def signatureId = column[Long]("signatureId")
  def methodFK = foreignKey("fk_sig_id", signatureId, TableQuery[MethodTable])(_.id, ForeignKeyAction.Cascade)

  override def * : ProvenShape[MethodChange] = (dateOfChange, authorName, branchName, signatureId).mapTo[MethodChange]
}