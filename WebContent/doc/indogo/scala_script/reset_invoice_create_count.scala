#!/bin/sh
exec /root/scala-2.11.8/bin/scala -classpath "/usr/local/apache-tomcat-8.5.12/lib/*" "$0" "/home/tomcat/prod/webapps/"
goto :eof
!#

Class.forName("com.mysql.jdbc.Driver")
import java.sql._
import scala.collection.mutable.ListBuffer
import java.io._
import scala.xml._

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

val conn = DriverManager.getConnection(f"jdbc:mysql://$dbIp%s:$dbPort%s/$dbInstance%s?useUnicode=true&characterEncoding=Big5", dbAccount, dbPassword)
try {
    conn.setAutoCommit(false)

    val stmt = conn.createStatement
    try {
        val rs = stmt.executeQuery("select config_value from global_config where group_name = 'money_transfer_ibon' and config_name = 'total_create_count' for update")
        try {
            rs.next
        } finally {
            rs.close
        }

        stmt.executeUpdate("update global_config set config_value = '0' where group_name = 'money_transfer_ibon' and config_name = 'total_create_count'")
    } finally {
        stmt.close
    }

    conn.commit
} catch {
    case e: Exception => {
        conn.rollback
        throw e
    }
} finally {
    conn.close
}