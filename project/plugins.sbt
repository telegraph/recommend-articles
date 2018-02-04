resolvers += "mvn-artifacts" atS3 "s3://mvn-artifacts/release"

addSbtPlugin("com.typesafe.play"  % "sbt-plugin"           % "2.6.0")
addSbtPlugin("uk.co.telegraph"    % "sbt-pipeline-plugin"  % "1.1.0-b+")
addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver"   % "0.14.0")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"        % "1.5.1")
addSbtPlugin("com.eed3si9n"       % "sbt-assembly"         % "0.14.6")
addSbtPlugin("net.virtual-void"   % "sbt-dependency-graph" % "0.9.0")
