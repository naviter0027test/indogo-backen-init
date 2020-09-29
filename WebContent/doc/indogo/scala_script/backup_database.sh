#!/bin/sh
exec /root/scala-2.11.8/bin/scala -classpath "/data/backup/lib/*" "$0" "/data/backup/conf/web.xml" "/data/backup/temp"
goto :eof
!#

val tableNames = Array[String](
"role_name_list", "role_menu", "session_list", "user_list", "user_role", "global_config", "history", "history_data",
"member", "bank_code_list", "member_recipient", "kurs_history", "money_transfer", "money_transfer_status", "idr_history", "usd_account", "usd_history", "member_point", "member_point_birthday", "bni_key_store", "member_point_schedule", "mini_mart_store", "send_sms_record")

Class.forName("com.mysql.jdbc.Driver")
import java.sql.DriverManager
import scala.xml.XML
import java.io.File
import java.io.PrintWriter
import scala.collection.mutable.ListBuffer
import java.sql.Types

val xmlFile = new File(args(0))
val tempDir = args(1)

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
        val allColumnTypes = tableNames.flatMap(tableName => {
            val r = stmt.executeQuery(f"select * from $tableName%s")
            try {
                val meta = r.getMetaData
                val list = ListBuffer[Int]()
                for (i <- 1 to meta.getColumnCount) {
                    list += (meta.getColumnType(i))
                }
                list.toList
            } finally {
                r.close
            }
        }).toSet
        
        val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    
        tableNames.foreach(tableName => {
            println(sdf.format(new java.util.Date) + " [INFO] " + tableName)
            val file = new File(tempDir, f"$tableName%s.tsv")
            val pw = new PrintWriter(file)
            try {
                val r = stmt.executeQuery(f"select * from $tableName%s")
                try {
                    val meta = r.getMetaData
                    val columnTypes = {
                        val list = ListBuffer[(Int, Int)]()
                        for (i <- 1 to meta.getColumnCount) {
                            list += ((i, meta.getColumnType(i)))
                        }
                        list.toList
                    }
                    
                    while (r.next) {
                        columnTypes.foreach(c => {
                            val columnIndex = c._1
                            val columnType = c._2
                            
                            if (columnIndex > 1) {
                                pw.write("\t")
                            }
                            
                            columnType match {
                                case Types.VARCHAR | Types.CHAR | Types.LONGVARCHAR => {
                                    val v = r.getString(columnIndex)
                                    if (v == null) pw.write("") else pw.write(v.replaceAll("\t", "\\\\t").replaceAll("\r", "").replaceAll("\n", "\\\\n"))
                                }
                                case Types.INTEGER | Types.SMALLINT | Types.BIT | Types.TINYINT => {
                                    val v = r.getInt(columnIndex)
                                    if (r.wasNull) pw.write("") else pw.write(v.toString)
                                }
                                case Types.DOUBLE | Types.DECIMAL => {
                                    val v = r.getDouble(columnIndex)
                                    if (r.wasNull) pw.write("") else pw.write(v.toString)
                                }
                                case Types.BIGINT => {
                                    val v = r.getLong(columnIndex)
                                    if (r.wasNull) pw.write("") else pw.write(v.toString)
                                }
                                case Types.TIMESTAMP => {
                                    val v = r.getTimestamp(columnIndex)
                                    if (r.wasNull) pw.write("") else pw.write(sdf.format(v))
                                }
                                case _ => {
                                    val v = r.getString(columnIndex)
                                    if (v == null) pw.write("") else pw.write(v)
                                }
                            }
                        })
                        pw.println()
                    }
                } finally {
                    r.close
                }
            } finally {
                pw.close
            }
        })
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
