import com.typesafe.config.ConfigFactory

lazy val scalaVersion = "2.13.4"
lazy val akkaVersion    = "2.6.12"
lazy val slickVersion = "3.3.3"
lazy val postgresVersion = "42.2.18"

lazy val root = (Project("ToDoList", file("."))
  settings(
  name := "todolist",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "org.postgresql" % "postgresql" % postgresVersion,
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.slick" %% "slick-codegen" % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
  )))

lazy val slickCodeGenTask = taskKey[Seq[File]]("slick code generation")

slickCodeGenTask := {
  val conf = ConfigFactory.parseFile(new File("src/main/resources/application.conf")).resolve()
  val outputDir = "src/main/scala"//(//dir. / "slick").getPath
  val pkg = "com.todolist.shared"
  val user = conf.getString("my.myDb.properties.user")
  val password = conf.getString("my.myDb.properties.password")
  val fname = outputDir + s"/$pkg/Tables.scala"
  (runner in Compile).value.run("slick.codegen.SourceCodeGenerator",
    (dependencyClasspath in Compile).value.files,
    Array(conf.getString("my.myDb.slickDriver"),
      conf.getString("my.myDb.driver"),
      conf.getString("my.myDb.properties.url"),
      outputDir, pkg, user, password)
    ,streams.value.log).failed.foreach(sys error _.getMessage)
  Seq(file(fname))
}
