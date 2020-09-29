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
        val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        println("truncate tables")
        tableNames.zipWithIndex.sortBy(_._2 * -1).foreach(x => {
            val tableName = x._1
            stmt.executeUpdate(f"delete from $tableName%s")
            println(sdf.format(new java.util.Date) + " [INFO] " + tableName)
        })
        conn.commit
    
        println("restore")
        tableNames.foreach(tableName => {
            println(sdf.format(new java.util.Date) + " [INFO] " + tableName)
            val file = new File(tempDir, f"$tableName%s.tsv")
            val br = new java.io.BufferedReader(new java.io.FileReader(file))
            try {
                    val columnTypes = {
                        val r = stmt.executeQuery(f"select * from $tableName%s limit 1")
                        val meta = r.getMetaData
                        try {
                            val list = ListBuffer[(Int, Int, String)]()
                            for (i <- 1 to meta.getColumnCount) {
                                list += ((i, meta.getColumnType(i), meta.getColumnName(i)))
                            }
                            list.toList
                        } finally {
                            r.close
                        }
                    }

                    val cols = columnTypes.map(_._3).mkString(",")
                    val vals = columnTypes.map(x => "?").mkString(",")
                    val pstmt = conn.prepareStatement(f"insert into $tableName%s ($cols%s) values ($vals%s)")
                    
                    var line = br.readLine
                    while (line != null) {
                        try {
                        val cells = org.apache.commons.lang3.StringUtils.splitPreserveAllTokens(line, '\t')
                        columnTypes.foreach(c => {
                            val columnIndex = c._1
                            val columnType = c._2
                            val v: String = if (columnIndex <= cells.length) cells(columnIndex - 1) else null
                            
                            if (v == null || v.length == 0) {
                                pstmt.setNull(columnIndex, columnType)
                            } else {
                                columnType match {
                                    case Types.VARCHAR | Types.CHAR | Types.LONGVARCHAR => {
                                        pstmt.setString(columnIndex, v.replaceAll("\\\\t", "\t").replaceAll("\\\\n", "\n"))
                                    }
                                    case Types.INTEGER | Types.SMALLINT | Types.BIT | Types.TINYINT => {
                                        pstmt.setInt(columnIndex, v.toInt)
                                    }
                                    case Types.DOUBLE | Types.DECIMAL => {
                                        pstmt.setDouble(columnIndex, v.toDouble)
                                    }
                                    case Types.BIGINT => {
                                        pstmt.setLong(columnIndex, v.toLong)
                                    }
                                    case Types.TIMESTAMP => {
                                        pstmt.setTimestamp(columnIndex, new java.sql.Timestamp(sdf.parse(v).getTime))
                                    }
                                    case _ => {
                                        pstmt.setString(columnIndex, v)
                                    }
                                }
                            }
                        })
                        pstmt.executeUpdate
                        } catch {
                            case e: Exception => {
                                println(line)
                                throw e
                            }
                        }
                        line = br.readLine
                    }
                    pstmt.close
            } finally {
                br.close
            }
            conn.commit
        })
    } finally {
        stmt.close
    }
} catch {
    case e: Exception => {
        conn.rollback
        throw e
    }
} finally {
    conn.close
}
