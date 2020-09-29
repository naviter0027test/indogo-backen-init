#!/bin/sh
exec /root/scala-2.11.8/bin/scala -classpath "/usr/local/apache-tomcat-8.5.12/lib/*:/home/tomcat/indogo/webapps/indogo_console_1.4_svn_535/WEB-INF/lib/indogo.jar" "$0" "/home/tomcat/indogo/webapps/"
goto :eof
!#

Class.forName("com.mysql.jdbc.Driver")
import java.sql._
import scala.collection.mutable.ListBuffer
import java.io._
import scala.xml._
import java.util.Calendar
import com.lionpig.webui.database.MySqlConnection
import com.indogo.bri.BriHostToHost

val workDir = args(0)

val xmlRoot = XML.loadFile(f"$workDir%s/ROOT/WEB-INF/web.xml")
val url = ((xmlRoot \\ "context-param").filter(n => (n.child \\ "param-name").text == "URL").head.child \\ "param-value").text
val tokens = url.split("/")
val folderName = tokens(tokens.length - 1)
val xmlFile = new File(new File(new File(workDir, folderName), "WEB-INF"), "web.xml")

val xml = XML.loadFile(xmlFile)
val dbIp = ((xml \\ "context-param").filter(n => (n.child \\ "param-name").text == "DB_IP").head.child \\ "param-value").text
val dbPort = ((xml \\ "context-param").filter(n => (n.child \\ "param-name").text == "DB_PORT").head.child \\ "param-value").text
val dbInstance = ((xml \\ "context-param").filter(n => (n.child \\ "param-name").text == "DB_INSTANCE").head.child \\ "param-value").text
val dbAccount = ((xml \\ "context-param").filter(n => (n.child \\ "param-name").text == "DB_ACCOUNT").head.child \\ "param-value").text
val dbPassword = ((xml \\ "context-param").filter(n => (n.child \\ "param-name").text == "DB_PASSWORD").head.child \\ "param-value").text

val conn = new MySqlConnection(dbIp, dbPort, dbInstance, dbAccount, dbPassword)
try {
    val bri = new BriHostToHost(conn)
    val token = bri.requestToken
    val accountName = bri.inquiryAccount(token, "", "")
    println(accountName)
} finally {
    conn.close
}
