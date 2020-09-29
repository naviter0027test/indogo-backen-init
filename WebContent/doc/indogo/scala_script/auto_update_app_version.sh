#!/bin/sh
exec /root/scala-2.11.8/bin/scala -classpath "/usr/local/apache-tomcat-8.5.12/lib/*" "$0" "/home/tomcat/prod/webapps/"
goto :eof
!#

Class.forName("com.mysql.jdbc.Driver")
import java.sql._
import java.io._
import java.net._
import scala.xml._

var app_version: String = null
val googlePlayStore = new URL("https://play.google.com/store/apps/details?id=tw.com.indogo.indogoremit");
val in = new BufferedReader(new InputStreamReader(googlePlayStore.openStream()))
try {
    var lineNumber = 0
    while (lineNumber < 100) {
        val line = in.readLine
        if (line == null) {
            lineNumber = 100
        } else {
            val idx = line.indexOf("itemprop=\"softwareVersion\"")
            if (idx >= 0) {
                val startIdx = line.indexOf(">", idx)
                val endIdx = line.indexOf("<", idx)
                if (startIdx >= 0 && endIdx >= 0) {
                    val version = line.substring(startIdx + 1, endIdx).trim
                    val dotIdx = version.indexOf('.')
                    if (dotIdx >= 0) {
                        val majorVersion = Integer.parseInt(version.substring(0, dotIdx))
                        val minorVersion = Integer.parseInt(version.substring(dotIdx + 1))
                        app_version = majorVersion + "." + minorVersion
                    }
                }
            }
        }
        lineNumber = lineNumber + 1
    }
} finally {
    in.close();
}

if (app_version != null) {
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
        val pstmt = conn.prepareStatement("update global_config set config_value = ? where group_name = 'app_share' and config_name = 'app_version'")
        val pstmtInsert = conn.prepareStatement("insert into global_config (group_name, config_name, config_value) values ('app_share', 'app_version', ?)")
        try {
            val config_value = {
                val r = stmt.executeQuery("select config_value from global_config where group_name = 'app_share' and config_name = 'app_version'")
                try {
                    if (r.next) {
                        r.getString(1)
                    } else {
                        null
                    }
                } finally {
                    r.close
                }
            }

            val current_time = {
                val r = stmt.executeQuery("select sysdate()")
                try {
                    r.next
                    r.getTimestamp(1)
                } finally {
                    r.close
                }
            }

            if (config_value == null) {
                pstmtInsert.setString(1, app_version)
                pstmtInsert.executeUpdate

                val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                println(sdf.format(current_time) + " insert " + app_version)
            } else if (config_value != app_version) {
                pstmt.setString(1, app_version)
                pstmt.executeUpdate

                val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                println(sdf.format(current_time) + " update from " + config_value + " to " + app_version)
            }
        } finally {
            pstmt.close
            pstmtInsert.close
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
}
