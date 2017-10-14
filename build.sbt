lazy val api = (project.in(file(".")))
  .settings(name := "api")
  .settings(baseSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      finchDependencies,
      commonDependencies,
      dbDependencies,
      iroboCommonDependencies,
      testDependencies,
      liquibaseDependencies,
      monixDependencies
    ).flatten
  )

lazy val appVersion = "0.1.0-SNAPSHOT"

lazy val compilerOptions = Seq(
  "-deprecation",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:higherKinds"
)

lazy val baseSettings = {
  val repositories = Seq(
    "twttr" at "https://maven.twttr.com/",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    "Sonatype Nexus Releases"  at "http://nexus.irobo.co.kr:8081/repository/maven-releases",
    "Sonatype Nexus Snapshots" at "http://nexus.irobo.co.kr:8081/repository/maven-snapshots"
  )

  Seq(
    version                   := appVersion,
    organization              := "irobo",
    scalaVersion              := "2.11.8",
    scalacOptions             := compilerOptions,
		testFrameworks += new TestFramework("utest.runner.Framework"),
    fork in test              := true,
    parallelExecution in Test := false,
    logBuffered               := false,
    credentials               += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    resolvers                 ++= repositories
  )
}

lazy val finchVersion              = "0.10.0"
lazy val scalaTestVersion          = "2.2.6"
lazy val mysqlConnectorVersion     = "5.1.38"
lazy val logBackVersion            = "1.1.3"
lazy val circeVersion              = "0.4.1"
lazy val jwtVersion                = "0.7.1"
lazy val mockitoVersion            = "1.10.19"
lazy val authentikatJWTVersion     = "0.4.1"
lazy val scalaBcryptVersion        = "2.6"
lazy val iroboCommonVersion        = "1.0.15"
lazy val liquibaseVersion          = "3.3.5"
lazy val liquibaseSlf4jVersion     = "1.2.1"
lazy val typesafeConfigVersion     = "1.3.0"
lazy val jodaTimeVersion           = "2.9.1"
lazy val jodaConvertVersion        = "1.8.1"
lazy val javaMailApiVersion        = "1.4.7"
lazy val finagleVersion            = "6.35.0"
lazy val quartzVersion             = "2.1.0"
lazy val utestVersion              = "0.4.3"
lazy val shapelessVersion          = "2.3.1"
lazy val catsVersion               = "0.6.0"
lazy val mysqlAsyncVersion         = "0.2.19"
lazy val monixVersion              = "2.1.2"
lazy val twitterServerVersion      = "1.20.0"
lazy val iroboDBLibraryVersion     = "1.0.4"

lazy val finagleDependencies = Seq(
  "com.twitter" %% "finagle-http" % finagleVersion
)

lazy val typeLevelDependencies = Seq(
  "com.chuusai"    %% "shapeless" % shapelessVersion,
  "org.typelevel"  %% "cats"      % catsVersion
)

lazy val logDependencies = Seq(
  // The logback-classic also pulls slf4j and logback-core
  "ch.qos.logback" % "logback-classic" % logBackVersion,
  "javax.mail"     % "mail"            % javaMailApiVersion
)

lazy val mysqlAsyncDependencies = Seq(
  "com.github.mauricio" %% "mysql-async" % mysqlAsyncVersion
)

lazy val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

lazy val monixDependencies = Seq(
  "io.monix" %% "monix-eval" % monixVersion,
  "io.monix" %% "monix-reactive" % monixVersion
)

lazy val twitterServerDependencies = Seq(
  "com.twitter" %% "twitter-server" % twitterServerVersion
)

lazy val commonDependencies = logDependencies ++ commonUtilDependencies

lazy val commonModuleDependencies = Seq(
  "org.quartz-scheduler" % "quartz" % quartzVersion
) ++ testDependencies

lazy val iroboDependencies = Seq(
  "irobo" %% "common"     % iroboCommonVersion,
  "irobo" %% "db-library" % iroboDBLibraryVersion
)

lazy val finchDependencies = Seq(
  "com.github.finagle" %% "finch-core"      % finchVersion,
  "com.github.finagle" %% "finch-circe"     % finchVersion,
  "io.circe"           %% "circe-generic"   % circeVersion,
  "io.circe"           %% "circe-core"      % circeVersion,
  "io.circe"           %% "circe-parser"    % circeVersion,
  "io.circe"           %% "circe-optics"    % circeVersion,
  "com.jason-goodwin"  %% "authentikat-jwt" % authentikatJWTVersion,
  "com.pauldijou"      %% "jwt-circe"       % jwtVersion,
  "com.github.t3hnar"  %% "scala-bcrypt"    % scalaBcryptVersion
)

lazy val dbDependencies = Seq(
  "mysql"               %  "mysql-connector-java" % mysqlConnectorVersion,
  "com.github.mauricio" %% "mysql-async"          % mysqlAsyncVersion
)

lazy val commonUtilDependencies = Seq(
  "com.typesafe" % "config"       % typesafeConfigVersion,
  "joda-time"    % "joda-time"    % jodaTimeVersion,
  "org.joda"     % "joda-convert" % jodaConvertVersion
)

lazy val testDependencies = Seq(
  "com.github.finagle" %% "finch-test"  % finchVersion     % "test",
  "org.scalatest"      %% "scalatest"   % scalaTestVersion % "test",
  "org.mockito"         % "mockito-all" % mockitoVersion,
	"com.lihaoyi" %% "utest" % utestVersion
)

lazy val iroboCommonDependencies = Seq(
  "irobo" %% "common" % iroboCommonVersion   
)

lazy val liquibaseDependencies = Seq(
  "org.liquibase"     % "liquibase-core"  % liquibaseVersion,
  "com.mattbertolini" % "liquibase-slf4j" % liquibaseSlf4jVersion
)
