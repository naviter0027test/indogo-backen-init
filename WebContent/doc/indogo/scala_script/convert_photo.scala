// scala -classpath "C:\work\eclipse-jee-neon-1a-win32-x86_64-workspace\indogo\indogo_console\WebContent\WEB-INF\lib\*;C:\work\eclipse-jee-neon-1a-win32-x86_64-workspace\indogo\org.jar"

Class.forName("com.mysql.jdbc.Driver")
import java.sql._
import scala.collection.mutable.ListBuffer

val conn = DriverManager.getConnection("jdbc:mysql://192.168.1.65:3306/indogo?useUnicode=true&characterEncoding=Big5", "indogo", "indogo#go#")
conn.setAutoCommit(false)

val stmt = conn.createStatement

val list = {
    val list = ListBuffer[(Long, String, String, String)]()
    val rs = stmt.executeQuery("select member_id, old_phone_no, old_arc_photo, old_signature_photo from member")
    try {
       while (rs.next) {
           val memberId = rs.getLong(1)
           val phoneNo = rs.getString(2)
           val arc = rs.getString(3)
           val signature = rs.getString(4)
           list += ((memberId, phoneNo, arc, signature))
        }
    } finally {
        rs.close
    }
    list.toList
}

import org.apache.commons.lang3.StringUtils
val newPhoneList = list.map(x => {
    val memberId = x._1
    val phoneNo = x._2
    val tokens = StringUtils.split(phoneNo, " ,")
    (memberId, "." + tokens.mkString("."))
})

newPhoneList.foreach(x => {
    val memberId = x._1
    val phoneNo = x._2
    stmt.executeUpdate(f"update member set phone_no = '$phoneNo%s' where member_id = $memberId%s")
})
conn.commit

val arcBaseDir = "C:\\Users\\ythung1\\Pictures\\henry camera"
val sigBaseDir = "C:\\Users\\ythung1\\Pictures\\henry camera"
val arcTargetDir = "C:\\Users\\ythung1\\Pictures\\henry camera\\target\\arc"
val sigTargetDir = "C:\\Users\\ythung1\\Pictures\\henry camera\\target\\sign"

import java.io._
import org.apache.commons.io.FileUtils
import javax.imageio.ImageIO
import org.imgscalr.Scalr
val rows = list.map(x => {
    val memberId = x._1
    val arcFilename = x._3
    val sigFilename = x._4
    val arcFile = new File(arcBaseDir, arcFilename)
    val sigFile = new File(sigBaseDir, sigFilename)
    (memberId, arcFile, sigFile)
})

rows.filter(_._2.exists).map(x => {
    val memberId = x._1
    val arcFile = x._2
    val level = memberId % 5000
    val targetDir = new File(new File(arcTargetDir, level.toString), memberId.toString)
    if (targetDir.exists) {
        FileUtils.deleteQuietly(targetDir)
    }
    targetDir.mkdirs()
    
    val currentTime = System.currentTimeMillis()
    val arc_photo_basename = f"$memberId%s_$currentTime%s"    
    val inputStream = new FileInputStream(arcFile)
    try {
        val bufferedImage = ImageIO.read(inputStream)
        val file = new File(targetDir, f"$arc_photo_basename%s_original.png")
        ImageIO.write(bufferedImage, "png", file)
        ImageIO.write(Scalr.resize(bufferedImage, 320, 240), "png", new File(targetDir, f"$arc_photo_basename%s_320_240.png"))
    } finally {
        inputStream.close
    }
    
    (memberId, arc_photo_basename)
}).foreach(x => {
    val memberId = x._1
    val name = x._2
    stmt.executeUpdate(f"update member set arc_photo_basename = '$name%s' where member_id = $memberId%s")
})

rows.filter(_._3.exists).map(x => {
    val memberId = x._1
    val sigFile = x._3
    val level = memberId % 5000
    val targetDir = new File(new File(sigTargetDir, level.toString), memberId.toString)
    if (targetDir.exists) {
        FileUtils.deleteQuietly(targetDir)
    }
    targetDir.mkdirs()
    
    val currentTime = System.currentTimeMillis()
    val sig_photo_basename = f"$memberId%s_$currentTime%s"
    val inputStream = new FileInputStream(sigFile)
    try {
        val bufferedImage = ImageIO.read(inputStream)
        val file = new File(targetDir, f"$sig_photo_basename%s_original.png")
        ImageIO.write(bufferedImage, "png", file)
        ImageIO.write(Scalr.resize(bufferedImage, 200, 30), "png", new File(targetDir, f"$sig_photo_basename%s.png"))
    } finally {
        inputStream.close
    }
    
    (memberId, sig_photo_basename)
}).foreach(x => {
    val memberId = x._1
    val name = x._2
    stmt.executeUpdate(f"update member set signature_photo_basename = '$name%s' where member_id = $memberId%s")
})

conn.commit
