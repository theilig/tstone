name := "TStone"
 
version := "1.0"
      
lazy val `tstone` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.13.2"

libraryDependencies ++= Seq( ehcache , ws , specs2 % Test , guice )

libraryDependencies ++= Seq("mysql" % "mysql-connector-java" % "5.1.49")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"
)

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "8.0.1"

libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "8.0.1"

libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"

libraryDependencies += "com.nimbusds" % "nimbus-jose-jwt" % "8.19"

libraryDependencies += specs2 % Test

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.5",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

unmanagedResourceDirectories in Test +=  baseDirectory ( _ /"target/web/public/test" ).value

