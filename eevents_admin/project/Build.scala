/*
 *   Copyright 2013 Ant Kutschera
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "eevents_admin"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        jdbc,
        anorm,
    	"com.typesafe" % "slick_2.10" % "1.0.0-RC1",
    	"org.slf4j" % "slf4j-nop" % "1.6.4",
    	"org.codehaus.btm" % "btm" % "2.1.3",
    	"commons-codec" % "commons-codec" % "1.7",
    	"com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % "1.3.2",
    	"javax.transaction" % "jta" % "1.1",
    	"org.jboss.spec.javax.jms" % "jboss-jms-api_1.1_spec" % "1.0.1.Final",
        "org.hornetq" % "hornetq-ra" % "2.2.21.Final",
        "org.hornetq" % "hornetq-jms" % "2.2.21.Final",
        "org.hornetq" % "hornetq-core" % "2.2.21.Final",
        "org.hornetq" % "hornetq-core-client" % "2.2.21.Final",
        "org.hornetq" % "hornetq-jms-client" % "2.2.21.Final",
        "org.apache.lucene" % "lucene-core" % "3.6.2",
        "mysql" % "mysql-connector-java" % "5.1.22",
        "org.neo4j" % "neo4j-cypher" % "1.9.M03",
        "org.neo4j" % "neo4j-graph-algo" % "1.9.M03",
        "org.neo4j" % "neo4j-graph-matching" % "1.9.M03",
        "org.neo4j" % "neo4j-jmx" % "1.9.M03",
        "org.neo4j" % "neo4j-kernel" % "1.9.M03",
        "org.neo4j" % "neo4j-lucene-index" % "1.9.M03",
        "org.neo4j" % "neo4j-shell" % "1.9.M03",
        "org.neo4j" % "neo4j-udc" % "1.9.M03",
        "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.jline" % "0.9.94_1",
        "org.neo4j" % "server-api" % "1.9.M03",
        "org.jsoup" % "jsoup" % "1.7.2",
        "org.jboss.as" % "jboss-as-ejb-client-bom" % "7.1.0.Final",
        "org.jboss.as" % "jboss-as-jms-client-bom" % "7.1.0.Final"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
        // Add your own project settings here      
    )

}
