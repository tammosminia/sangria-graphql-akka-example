resolvers += Resolver.sonatypeRepo("public")

lazy val akkaHttpVersion                 = "10.2.6"
lazy val akkaVersion                     = "2.6.17"
lazy val akkaHttpCirceVersion            = "1.38.2"
lazy val circeVersion                    = "0.14.1"
lazy val scalaTestVersion                = "3.1.2"
lazy val sangriaVersion                  = "2.1.3"
lazy val sangriaCirceVersion             = "1.3.2"

lazy val akkaDeps = Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-http"        % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream"      % akkaVersion
)

lazy val sangriaDeps = Seq(
  "org.sangria-graphql" %% "sangria"              % sangriaVersion,
  "org.sangria-graphql" %% "sangria-circe"        % sangriaCirceVersion,
  "org.sangria-graphql" %% "sangria-akka-streams" % "1.0.2"
)

lazy val serializationDeps = Seq(
  "io.circe"          %% "circe-core"           % circeVersion,
  "io.circe"          %% "circe-generic"        % circeVersion,
  "io.circe"          %% "circe-parser"         % circeVersion,
  "io.circe"          %% "circe-generic-extras" % circeVersion,
  "de.heikoseeberger" %% "akka-http-circe"      % akkaHttpCirceVersion
)

lazy val testDeps = Seq(
  "org.scalatest"           %% "scalatest"                % "3.1.0"     % Test,
  "com.typesafe.akka"       %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.softwaremill.sttp.client3" %% "core" % "3.3.13",
  "com.softwaremill.sttp.client3" %% "akka-http-backend" % "3.3.13"
)

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        scalaVersion := "2.13.2"
      )
    ),
    name := "graphql subscriptions in akka-http",
    libraryDependencies ++= Seq(
      akkaDeps,
      sangriaDeps,
      serializationDeps,
      testDeps
    ).flatten,
    scalacOptions ++= Seq(
      "-language:postfixOps",
      "-language:implicitConversions"
    )
  )
