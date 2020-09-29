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
        val txn_ids = {
            val list = ListBuffer[Long]()
            val rs = stmt.executeQuery("select txn_id from money_transfer where payment_id = 2 and transfer_status_id = 1 and lm_time_expire < date_sub(sysdate(), interval 900 second)")
            try {
                while (rs.next) {
                    list += (rs.getLong(1))
                }
            } finally {
                rs.close
            }
            list.toList.sortBy(identity)
        }

        val lm_time = {
            val rs = stmt.executeQuery("select sysdate()")
            try {
                rs.next
                val dateFormat = new java.text.SimpleDateFormat("yyyyMMddHHmmss")
                new java.sql.Timestamp(dateFormat.parse(dateFormat.format(rs.getTimestamp(1))).getTime)
            } finally {
                rs.close
            }
        }

        val pstmt = conn.prepareStatement("insert into money_transfer_status (txn_id, lm_time, lm_user, old_status_id, new_status_id, comment) values (?,?,?,?,?,?)")
        val pstmtStatus = conn.prepareStatement("update money_transfer set transfer_status_id = ?, lm_time = ?, lm_user = ? where txn_id = ?")
        val pstmtSearch = conn.prepareStatement("select lm_time from member_point where member_id = ? and txn_id = ? and reason_id = 4 order by lm_time desc")
        val pstmtAddPoint = conn.prepareStatement("update member set remit_point = remit_point + ? where member_id = ?")
        val pstmtDeletePointHistory = conn.prepareStatement("delete from member_point where member_id = ? and lm_time = ?")
        val pstmtMember = conn.prepareStatement("select transfer_amount_ntd from member where member_id = ? for update")
        val pstmtUpdateTotalNtd = conn.prepareStatement("update member set transfer_amount_ntd = ? where member_id = ?")
        val pstmtGetTotalCreate = conn.prepareStatement("select config_value from global_config where group_name = 'money_transfer_ibon' and config_name = 'total_create_count' for update")
        val pstmtSetTotalCreate = conn.prepareStatement("update global_config set config_value = ? where group_name = 'money_transfer_ibon' and config_name = 'total_create_count'")
        try {
            txn_ids.foreach(txn_id => {
                val v = {
                    val rs = stmt.executeQuery(f"select transfer_status_id, point_used, member_id, transfer_amount_ntd from money_transfer where txn_id = $txn_id%s for update")
                    try {
                        if (rs.next) {
                            (rs.getInt(1), rs.getInt(2), rs.getLong(3), rs.getInt(4))
                        } else {
                            (-1, 0, 0L, 0)
                        }
                    } finally {
                        rs.close
                    }
                }

                val transfer_status_id = v._1
                val point_used = v._2
                val member_id = v._3
                val transfer_amount_ntd = v._4

                if (transfer_status_id == 1) {
                    pstmtMember.setLong(1, member_id)
                    var total_amount_ntd = {
                        val rs = pstmtMember.executeQuery
                        try {
                            rs.next
                            rs.getInt(1)
                        } finally {
                            rs.close
                        }
                    }

                    total_amount_ntd = total_amount_ntd - transfer_amount_ntd
                    if (total_amount_ntd < 0) {
                        total_amount_ntd = 0
                    }
                    pstmtUpdateTotalNtd.setLong(1, total_amount_ntd)
                    pstmtUpdateTotalNtd.setLong(2, member_id)
                    pstmtUpdateTotalNtd.executeUpdate

                    pstmt.setLong(1, txn_id)
                    pstmt.setTimestamp(2, lm_time)
                    pstmt.setString(3, "system")
                    pstmt.setInt(4, transfer_status_id)
                    pstmt.setInt(5, 6)
                    pstmt.setString(6, "expired")
                    pstmt.executeUpdate

                    pstmtStatus.setInt(1, 6)
                    pstmtStatus.setTimestamp(2, lm_time)
                    pstmtStatus.setString(3, "system")
                    pstmtStatus.setLong(4, txn_id)
                    pstmtStatus.executeUpdate

                    if (point_used > 0) {
                        var remit_lm_time: Timestamp = null
                        pstmtSearch.setLong(1, member_id)
                        pstmtSearch.setLong(2, txn_id)
                        val rs = pstmtSearch.executeQuery
                        try {
                            if (rs.next) {
                                remit_lm_time = rs.getTimestamp(1)
                            }
                        } finally {
                            rs.close
                        }

                        if (remit_lm_time != null) {
                            pstmtAddPoint.setInt(1, point_used)
                            pstmtAddPoint.setLong(2, member_id)
                            pstmtAddPoint.executeUpdate()

                            pstmtDeletePointHistory.setLong(1, member_id)
                            pstmtDeletePointHistory.setTimestamp(2, remit_lm_time)
                            pstmtDeletePointHistory.executeUpdate()
                        }
                    }

                    println(f"$txn_id%s")
                }

                var total_create_count = {
                    val rs = pstmtGetTotalCreate.executeQuery
                    try {
                        rs.next
                        rs.getString(1).toInt
                    } finally {
                        rs.close
                    }
                }

                total_create_count = total_create_count - 1
                if (total_create_count < 0) {
                    total_create_count = 0
                }

                pstmtSetTotalCreate.setString(1, total_create_count.toString)
                pstmtSetTotalCreate.executeUpdate

                conn.commit
            })
        } finally {
            pstmt.close
            pstmtStatus.close
            pstmtSearch.close
            pstmtAddPoint.close
            pstmtDeletePointHistory.close
            pstmtMember.close
            pstmtUpdateTotalNtd.close
            pstmtGetTotalCreate.close
            pstmtSetTotalCreate.close
        }
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
