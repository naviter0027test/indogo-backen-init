::#!
@echo off
call scala -classpath "%0\..\lib\*" "%0" "%0\ROOT\WEB-INF\web.xml" "%0"
goto :eof
::!#

Class.forName("com.mysql.jdbc.Driver")
import java.sql._
import scala.collection.mutable.ListBuffer
import java.io._
import scala.xml._

val xmlRootFilename = args(0)
val workDir = args(1)

val xmlRoot = XML.loadFile(xmlRootFilename)
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
        val expiredTransactionId = {
            val rs = stmt.executeQuery("select config_value from global_config where group_name = 'money_transfer_ibon' and config_name = 'expire_date'")
            try {
                if (rs.next) {
                    val expireDate = rs.getString(1).toInt
                    val calendar = java.util.Calendar.getInstance
                    calendar.add(java.util.Calendar.DATE, -expireDate)
                    val dateFormat = new java.text.SimpleDateFormat("yyyyMMdd")
                    dateFormat.format(calendar.getTime).toLong * 1000000
                } else {
                    -1
                }
            } finally {
                rs.close
            }
        }
        
        val txn_ids = {
            val list = ListBuffer[Long]()
            val rs = stmt.executeQuery(f"select txn_id from money_transfer where payment_id = 2 and transfer_status_id = 1 and txn_id < '$expiredTransactionId%s'")
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
        try {
            txn_ids.foreach(txn_id => {
                val transfer_status_id = {
                    val rs = stmt.executeQuery(f"select transfer_status_id from money_transfer where txn_id = $txn_id%s for update")
                    try {
                        if (rs.next) {
                            rs.getInt(1)
                        } else {
                            -1
                        }
                    } finally {
                        rs.close
                    }
                }
                
                if (transfer_status_id == 1) {
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
                    
                    println(f"$txn_id%s")
                }
                
                conn.commit
            })
        } finally {
            pstmt.close
            pstmtStatus.close
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