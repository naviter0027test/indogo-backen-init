#!/bin/sh
exec /root/scala-2.11.8/bin/scala -classpath "/usr/local/apache-tomcat-8.5.12/lib/*" "$0" "/home/tomcat/prod/webapps/"
goto :eof
!#

Class.forName("com.mysql.jdbc.Driver")
import java.sql._
import scala.collection.mutable.ListBuffer
import java.io._
import scala.xml._
import java.util.Calendar

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
    val pstmtMember = conn.prepareStatement("select member_id from member where month(birthday) = ? and day(birthday) = ? and is_wait_confirm in (3, 4)")
    val pstmtPointBirthday = conn.prepareStatement("select count(*) from member_point_birthday where member_id = ? and bonus_year = ?")
    val pstmtAddPoint = conn.prepareStatement("update member set remit_point = remit_point + ? where member_id = ?");
    val pstmtAddPointHist = conn.prepareStatement("insert into member_point (member_id, lm_time, remit_point, reason_id) values (?,?,?,8)");
    val pstmtAddBirthdayYear = conn.prepareStatement("insert into member_point_birthday (member_id, bonus_year) values (?,?)");
    try {
        val birthday_bonus = {
            val r = stmt.executeQuery("select config_value from global_config where group_name = 'remit_point' and config_name = 'birthday_bonus'")
            try {
                r.next
                r.getString(1).toInt
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
        
        println(current_time)
        
        val calendar = Calendar.getInstance
        calendar.setTime(current_time)
        val current_year = calendar.get(Calendar.YEAR)
        val current_month = calendar.get(Calendar.MONTH) + 1
        val current_date = calendar.get(Calendar.DAY_OF_MONTH)
        
        pstmtMember.setInt(1, current_month)
        pstmtMember.setInt(2, current_date)
        val member_ids = {
            val r = pstmtMember.executeQuery
            try {
                val list = ListBuffer[Long]()
                while (r.next) {
                    list += (r.getLong(1))
                }
                list.toList
            } finally {
                r.close
            }
        }
        
        member_ids.filter(member_id => {
            pstmtPointBirthday.setLong(1, member_id)
            pstmtPointBirthday.setInt(2, current_year)
            val r = pstmtPointBirthday.executeQuery
            try {
                r.next
                val count = r.getInt(1)
                count == 0
            } finally {
                r.close
            }
        }).foreach(member_id => {
            println(member_id)
        
            pstmtAddPoint.setInt(1, birthday_bonus)
            pstmtAddPoint.setLong(2, member_id)
            pstmtAddPoint.executeUpdate
            
            pstmtAddPointHist.setLong(1, member_id)
            pstmtAddPointHist.setTimestamp(2, current_time)
            pstmtAddPointHist.setInt(3, birthday_bonus)
            pstmtAddPointHist.executeUpdate
            
            pstmtAddBirthdayYear.setLong(1, member_id)
            pstmtAddBirthdayYear.setInt(2, current_year)
            pstmtAddBirthdayYear.executeUpdate
        })
    } finally {
        stmt.close
        pstmtMember.close
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