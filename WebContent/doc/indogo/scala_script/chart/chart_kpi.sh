#!/bin/sh
exec /root/scala-2.11.8/bin/scala -classpath "/usr/local/apache-tomcat-8.5.12/lib/*" "$0" "/home/tomcat/prod/webapps/" "2016-08-01"
goto :eof
!#

Class.forName("com.mysql.jdbc.Driver")
import java.sql._
import scala.collection.mutable.ListBuffer
import java.io._
import scala.xml._
import java.util.Calendar
import java.sql.Timestamp

val workDir = args(0)
val startDate = args(1)

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
        val scoreCreateInvoice = 40
        val scoreCreateMember = 60
        val scoreVerifyOldMember = 15
        val scoreVerifyNewMember = 90
        val scoreCreateSignature = 20
    
        val listCreateInvoice = {
            val r = stmt.executeQuery(f"select date(action_time), action_user, count(*) from history where table_id = 4 and action_time >= str_to_date('$startDate%s', '%%Y-%%m-%%d') and action_user <> 'app' and action_id = 1 group by date(action_time), action_user")
            try {
                val list = ListBuffer[(Timestamp, String, Int)]()
                while (r.next) {
                    list += ((r.getTimestamp(1), r.getString(2), r.getInt(3) * scoreCreateInvoice))
                }
                list.toList
            } finally {
                r.close
            }
        }
        
        val listCreateMember = {
            val r = stmt.executeQuery(f"select date(action_time), action_user, count(*) from history where table_id = 1 and action_time >= str_to_date('$startDate%s', '%%Y-%%m-%%d') and action_id = 1 group by date(action_time), action_user")
            try {
                val list = ListBuffer[(Timestamp, String, Int)]()
                while (r.next) {
                    list += ((r.getTimestamp(1), r.getString(2), r.getInt(3) * scoreCreateMember))
                }
                list.toList
            } finally {
                r.close
            }
        }
        
        val listCreateSignature = {
            val r = stmt.executeQuery(f"""SELECT action_time, action_user, COUNT(*)
FROM (
    SELECT (
           SELECT new_attr_value
             FROM history_data b
            WHERE b.log_id = a.log_id AND b.attr_name = 'signature_photo_basename') AS signature_photo_basename,
           a.action_user,
           DATE(a.action_time) AS action_time
      FROM history a
     WHERE a.table_id = 1 AND a.action_time >= STR_TO_DATE('$startDate%s', '%%Y-%%m-%%d') AND a.action_id = 2
) c
where c.signature_photo_basename not like '%%_app'
GROUP BY action_time, action_user""")
            try {
                val list = ListBuffer[(Timestamp, String, Int)]()
                while (r.next) {
                    list += ((r.getTimestamp(1), r.getString(2), r.getInt(3) * scoreCreateSignature))
                }
                list.toList
            } finally {
                r.close
            }
        }
        
        val listVerifyNewMember = {
            val r = stmt.executeQuery(f"select date(action_time), action_user, count(*) from history where table_id = 5 and action_time >= str_to_date('$startDate%s', '%%Y-%%m-%%d') and action_id = 2 and action_desc = 'new_member' group by date(action_time), action_user")
            try {
                val list = ListBuffer[(Timestamp, String, Int)]()
                while (r.next) {
                    list += ((r.getTimestamp(1), r.getString(2), r.getInt(3) * scoreVerifyNewMember))
                }
                list.toList
            } finally {
                r.close
            }
        }
        
        val listVerifyOldMember = {
            val r = stmt.executeQuery(f"select date(action_time), action_user, count(*) from history where table_id = 5 and action_time >= str_to_date('$startDate%s', '%%Y-%%m-%%d') and action_id = 2 and action_desc = 'old_member' group by date(action_time), action_user")
            try {
                val list = ListBuffer[(Timestamp, String, Int)]()
                while (r.next) {
                    list += ((r.getTimestamp(1), r.getString(2), r.getInt(3) * scoreVerifyOldMember))
                }
                list.toList
            } finally {
                r.close
            }
        }
        
        val listOverall = (listCreateInvoice ++ listCreateMember ++ listCreateSignature ++ listVerifyNewMember ++ listVerifyOldMember).groupBy(x => (x._1, x._2)).map(x => {
            (x._1._1, x._1._2, x._2.map(_._3).sum)
        }).toList
        
        val mapUser = {
            val pstmtUserRowId = conn.prepareStatement("select user_row_id from user_list where user_name = ?")
            val pstmtInsertSeries = conn.prepareStatement("insert into chart_series (chart_id, series_id, series_name) values (?, ?, ?)")
            try {
                val listUser = listOverall.map(_._2).distinct.map(userName => {
                    pstmtUserRowId.setString(1, userName)
                    val userRowId = {
                        val r = pstmtUserRowId.executeQuery
                        try {
                            if (r.next) {
                                r.getInt(1)
                            } else {
                                Double.NaN
                            }
                        } finally {
                            r.close
                        }
                    }
                    (userName, userRowId)
                }).filter(x => {
                    !java.lang.Double.isNaN(x._2)
                }).map(x => {
                    (x._1, x._2.toInt)
                })
                
                listUser.foreach(x => {
                    try {
                        for (i <- 1 to 6) {
                            pstmtInsertSeries.setInt(1, i)
                            pstmtInsertSeries.setInt(2, x._2)
                            pstmtInsertSeries.setString(3, x._1)
                            pstmtInsertSeries.executeUpdate
                        }
                    } catch {
                        case e: Exception =>
                    }
                })
                
                listUser.toMap
            } finally {
                pstmtUserRowId.close
            }
        }
        
        conn.commit
        
        val charts = scala.Array(
            (listCreateInvoice, 1),
            (listCreateMember, 2),
            (listVerifyNewMember, 3),
            (listVerifyOldMember, 4),
            (listCreateSignature, 5),
            (listOverall, 6)
        )
        
        val pstmt = conn.prepareStatement("insert into chart_daily (chart_id, series_id, point_id, point_value) values (?,?,?,?)")
        charts.foreach(x => {
            val list = x._1
            val chartId = x._2
            
            list.filter(y => {
                mapUser.contains(y._2)
            }).foreach(y => {
                val timestamp = y._1
                val userName = y._2
                val value = y._3
                val userRowId = mapUser(userName)
                
                pstmt.setInt(1, chartId)
                pstmt.setInt(2, userRowId)
                pstmt.setTimestamp(3, timestamp)
                pstmt.setDouble(4, value)
                try {
                    pstmt.executeUpdate
                } catch {
                    case e: Exception =>
                }
            })
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