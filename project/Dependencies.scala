import sbt._

object Dependencies {

  object Version {
    val apacheCommonsCollections = "4.3"
    val jfreeChart = "1.5.0"
    val sqlite = "3.28.0"
    val slick = "3.3.2"
    val scalaTest = "3.0.8"
    val liquibase = "3.8.0"
    val ideaVersion = "2018.3"
  }


  val apacheCommonsCollections = "org.apache.commons" % "commons-collections4" % Version.apacheCommonsCollections
  val jfreeChart = "org.jfree" % "jfreechart" % Version.jfreeChart
  val sqlite = "org.xerial" % "sqlite-jdbc" % Version.sqlite
  val slick = "com.typesafe.slick" %% "slick" % Version.slick
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % Test
  val liquibase = "org.liquibase" % "liquibase-core" % Version.liquibase

  lazy val topiasDependencies = Seq(apacheCommonsCollections, jfreeChart, sqlite, slick, liquibase)
}