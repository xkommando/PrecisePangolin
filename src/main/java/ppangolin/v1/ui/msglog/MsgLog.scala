package ppangolin.v1.ui.msglog

import java.text.{FieldPosition, SimpleDateFormat}
import java.util.{Calendar, Date}

/**
  * Created by Bowen Cai on 11/23/2015.
  */
object MsgLog {
  val longFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val shortFmt = new SimpleDateFormat("HH:mm:ss")
  val start0 = new FieldPosition(0)
  val todayStart = {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.getTime.getTime
  }
}

trait MsgLog {
  val time: Long
  val digest: String
  val toolTip: String

  import MsgLog._

  lazy val fmtTime: String = fmtTime(new StringBuffer(25)).toString
  def fmtTime(b: StringBuffer):StringBuffer = {
    if (time >= todayStart) { // today's logs
      shortFmt.format(new Date(time), b, start0)
    } else { // history logs
      longFmt.format(new Date(time), b, start0)
    }
    b
  }
}
case class TextMsgLog(isEncrypt:Boolean, plain:String,
                      cipher:String,
                      time:Long = System.currentTimeMillis())
  extends MsgLog {

  import MsgLog._
  lazy val toolTip = {
    if (isEncrypt)
      "Encrypted on: " + fmtTime
    else
      "Decrypted on: " + fmtTime
  }
  /**
    * digest of this message log, length around 24
    * if it is msg created today, display hour, minute, second, otherwise year month date hour minute
    */
  val digest = {
    val d = new StringBuffer(35)

    var take = 25
    if (time >= todayStart) { // today's logs
      shortFmt.format(new Date(time), d, start0)
      take -= 8
    } else { // history logs
      longFmt.format(new Date(time), d, start0)
      take -= 15
    }
    d.append("  ")
    take -= 2
    if (take >= plain.length())
      d.append(plain)
    else if (take > 4) {
      d.append(plain.substring(0, take - 4)).append(" ...")
    }
    d.toString()
  }
}
