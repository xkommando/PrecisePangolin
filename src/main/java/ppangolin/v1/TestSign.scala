package ppangolin.v1

import java.awt.{FlowLayout, Font, GraphicsEnvironment}
import java.io.{InputStreamReader, BufferedReader, FileInputStream, FileReader}
import java.security.Signature
import java.util.regex.Pattern
import java.util.{Date, Scanner}
import javax.swing.{JFrame, JLabel, JPanel}

import net.liftweb.json.{DefaultFormats, Extraction, JsonParser, TypeInfo}
import ppangolin.v1.misc.Str
import ppangolin.v1.sbox.{AsymBox, FullKeyPack, HySBox, SymBox}
import ppangolin.v1.ui.Activities._
import ppangolin.v1.ui.msglog.MsgLog

import scala.collection.mutable.ArrayBuffer
/**
 * Created by Bowen Cai on 11/12/2015.
 */
object TestSign {

  def start(): Unit = ???

  val path = "D:\\bcprov-jdk15on-153.jar"
  val pathKey = "C:\\Users\\Bowen Cai\\Documents\\mykyes_t1.json"

  def t1(): Unit = {

//    Security.addProvider(new BouncyCastleProvider())
    val hySBox = new HySBox(new AsymBox("RSA", "RSA/ECB/PKCS1Padding"), new SymBox("AES/ECB/PKCS5Padding", "AES"))

    val fp = Extraction.extract(JsonParser.parse(new FileReader(pathKey)), TypeInfo(classOf[FullKeyPack], None))(DefaultFormats).asInstanceOf[FullKeyPack]
//    hySBox.genMyKeyPair(1024)
    hySBox.setTheirPubKey(fp.theirPubKey.get.toCharArray)

//    hySBox.setTheirPubKey(hySBox.myPub)
//    val ds = Signature.getInstance("SHA256withRSA")
//    ds.initSign(hySBox.myPri)
//    processStream(new FileInputStream(path), (buf, len) => ds.update(buf, 0, len)).close()
//    val bs = ds.sign()

    val ds2 = Signature.getInstance("SHA256withRSA")
    ds2.initVerify(hySBox.theirPub)
    processStream(new FileInputStream(path), (buf, len)=>ds2.update(buf, 0, len)).close()
    var str = ""
    val sc = new Scanner(System.in)
    str = sc.nextLine()
    val bs = hySBox.B64Decoder.decode(str)
    val real = ds2.verify(bs)
    println(real)

  }

  def t2(): Unit = {
//    Security.addProvider(new BouncyCastleProvider())
    val hySBox = new HySBox(new AsymBox("RSA","RSA/ECB/PKCS1Padding"), new SymBox("AES/ECB/PKCS5Padding", "AES"))
    hySBox.genMyKeyPair(1024)
    val prikbs = hySBox.myPri.getEncoded

//    val _tp = Hex.decodeHex(Hex.encodeHex(prikbs))
    val _tp2 = Str.Codec.fromHexChars(Str.Codec.toHexChars(prikbs))

    val prikstrhex = Str.Codec.toHexStr(prikbs)
    val prikstrb64 = hySBox.B64Encoder.encodeToString(Str.Codec.fromHexStr(prikstrhex))

    val bs2 = hySBox.B64Decoder.decode(prikstrb64)

    println(hySBox)
  }
  def t3(): Unit ={

    val regexHexChar = Pattern.compile("[^a-zA-Z0-9+/=]") // filter input
//    def sanitizedHex(in:String):String = ??? //regexHexChar.matcher(in).replaceAll("")
    val b64 = "+WlS0/eX3l/+/U6Hv7A1gwFIwoa5+/+411=="
    val sa = regexHexChar.matcher(b64).replaceAll("")
    println(sa == b64)
  }

  def t4(): Unit = {
//    println("\u25B6")
//    println("\u2709")
//    println("\u27A4")
//    println("\u2196")
//    println("\u270A")
//    println("\u270B")

    val names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
    val f = new JFrame()
    val p = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 10))
    f.add(p)
    f.setSize(800, 800)
    for (n <- names) {
      val lb = new JLabel(n + " \u270b  蔡博文")
      lb.setFont(new Font(n, 1, 20))
      p.add(lb)
    }
    f.setVisible(true)

  }
  def t5(): Unit = {
    val a = ArrayBuffer(11, 8, 5, 4, 2, -1)
    val v = 3
    val i = a.indexWhere(_ < v)
    println(i)
    if (i >= 0)
      a.insert(i, v)
    else
      a.append(v)
    a.foreach(println)
  }

  val regexBinStr2 = Pattern.compile("[0|1]*") // filter input

  def t6(): Unit = {
//    val str = "6JSh5Y2a5paH"
//    println(regexBase64Str.matcher(str).matches())
    val t = System.currentTimeMillis() - MsgLog.todayStart
//    println(t.asInstanceOf[Float] / 86400000L)
    val d = new Date(1450635814116L)
    println(d)
    println(MsgLog.longFmt.format(d))
  }

  def t7(): Unit = {
    val proc = Runtime.getRuntime.exec("netstat", Array("-a","-n","-o"))
    val r = new BufferedReader(new InputStreamReader(proc.getInputStream))
    var str = r.readLine()
    while (str != null) {
      println(str)
      str = r.readLine()
    }
    r.close()
  }

  def main(args: Array[String]): Unit = {
    t7()
  }
}
