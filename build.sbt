import com.github.play2war.plugin._

name := "dijon"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.apache.solr" % "solr-solrj" % "4.6.0",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  javaJdbc,
  javaEbean,
  cache,
  filters,
  "mysql" % "mysql-connector-java" % "5.1.26",
  "commons-io" % "commons-io" % "2.4",
  "commons-lang" % "commons-lang" % "2.6",
  "org.json" % "json" % "20090211"
)

play.Project.playJavaSettings

Play2WarPlugin.play2WarSettings

Play2WarKeys.servletVersion := "3.0"
