package ppangolin.v1

import java.io.{File, FileInputStream, FileOutputStream}
import java.net.NetworkInterface
import java.security.Security

import ppangolin.v1.misc.Base64
import ppangolin.v1.sbox.{AsymBox, HySBox, KeyPack, SymBox}

/**
 * Created by Bowen Cai on 9/23/2015.
 */

object TestSBox extends App {


  rsa()
//  aes()
//  comb()
//  aesBench()
//  rsaBench()
  def aesBench() {
//    for ((i,j) <- mapAsScalaMapConverter(System.getenv()).asScala)
//      println(i + "  " + j)
//    for ((i,j) <- mapAsScalaMapConverter(System.getProperties()).asScala)
//      println(i + "  " + j)

//    for ((i,j) <- Runtime.getRuntime.)
//      println(i + "  " + j)

    //forEach((a:String,b:String)=>println(a + "  " + b))

    val path = "F:\\video\\bwe\\大西洋帝国 第1季\\大西洋帝国第1季第8集720p[江海互动论坛www.70king.com].rmvb"
//    val path = "E:\\__TEMP__\\SQLite-a611fa96c4a84861\\ext\\fts3\\unicode\\UnicodeData.txt"

    val in = new FileInputStream(path)
    val buf = new Array[Byte](8000)

    val box = new SymBox("AES", "AES/ECB/PKCS5Padding")
    box.genSecretKey(128)

    val t1 = System.currentTimeMillis()
    val out = box.encryptStream(new FileOutputStream(new File("D:\\en.rsa")))

    var len = in.read(buf)
    while (-1 != len) {
      out.write(buf, 0, len)
      len = in.read(buf)
    }
    in.close()
    out.close()

    val t2 = System.currentTimeMillis()
    box.secretKeyCA.foreach(print)
//    Q0smaNzMkrVa+fUqDTPN0g==

    val box2 = new SymBox("AES/ECB/PKCS5Padding", "AES")
    box2.setSecretKey(box.secretKeyCA)

    val in2 = box2.decryptStream(new FileInputStream("D:\\en.rsa"))
    val out2 = new FileOutputStream("D:\\en.txt")

    len = in2.read(buf)
    while (-1 != len) {
      out2.write(buf, 0, len)
      len = in2.read(buf)
    }
    in2.close()
    out2.close()
    val t3 = System.currentTimeMillis()

    println((t3 - t2) + "  " + (t2 - t1)) // 6091  8111
  }

//  def rsaBench(): Unit = {
//
//    val path = "E:\\__TEMP__\\SQLite-a611fa96c4a84861\\ext\\fts3\\unicode\\UnicodeData.txt"
//
//    val in = new FileInputStream(path)
//    val buf = new Array[Byte](8000)
//
//    val box = new RsaBox
//    box.genMyKeyPair(512)
//    box.setTheirPubKey(box.myPub)
//
//    val out = box.encryptStream(new FileOutputStream(new File("D:\\en.rsa")))
//
//    var len = in.read(buf)
//    while (-1 != len) {
//      out.write(buf, 0, len)
//      len = in.read(buf)
//    }
//    in.close()
//    out.close()
//  }

  def comb(): Unit ={

    val ayc1 = new AsymBox("RSA","RSA/ECB/PKCS1Padding")
    val ayc2 = new AsymBox("RSA","RSA/ECB/PKCS1Padding")
    val yc1 = new SymBox("AES", "AES/ECB/PKCS5Padding")
    val yc2 = new SymBox("AES", "AES/ECB/PKCS5Padding")

    val c1 = new HySBox(ayc1, yc1)
    val c2 = new HySBox(ayc2, yc2)

    c1.genMyKeyPair(512)
    c2.genMyKeyPair(1024)
    c1.genSecretKey(128)

    c1.setTheirPubKey(c2.myPubCA)
    c2.setTheirPubKey(c1.myPubCA)
    println(c1.eSecretKeyCA.length)
    c2.setSecretKey(c1.eSecretKeyCA)

    val msg = "fuck GWF"

    val cip = c1.encrypt(msg.getBytes("ASCII"))
    println(cip.length)
    val nm = new String(c2.decrypt(cip), "ASCII")
    println(nm)
  }


  def aes() = {
    val c1 = new SymBox("AES", "AES/ECB/PKCS5Padding")
    val c2 = new SymBox("AES", "AES/ECB/PKCS5Padding")

    c1.genSecretKey(128)
    c1.secretKeyCA.foreach(print)
    println()
    println(c1.secretKeyCA.length)
    c2.setSecretKey(c1.secretKey)

    val msg = "fuck GWF"
    val cip = c1.encrypt(msg.getBytes("ASCII"))
    println(cip.length)

    println(new String(c2.decrypt(cip), "ASCII"))

  }


  def rsa() = {

//    Security.addProvider(new BouncyCastleProvider())
//    val c1 = new AsymBox("RSA","RSA/ECB/PKCS1Padding")
//    val c2 = new AsymBox("RSA","RSA/ECB/PKCS1Padding")

    val c1 = new AsymBox("ElGamal","ElGamal")
    val c2 = new AsymBox("ElGamal","ElGamal")

    c1.genMyKeyPair(512)
    c2.genMyKeyPair(1024)

    val c1pub = c1.myPubCA
    val c2pub = c2.myPubCA

    c1pub.foreach(print)
    println()
    c2pub.foreach(print)
    println()

    c1.setTheirPubKey(c2pub)
    c2.setTheirPubKey(c1pub)

    println(c2pub.length)
    val msg = "fuck GWF"

    var cip = c1.encrypt(msg.getBytes("ASCII"))
    println(cip.length)
    var nm = new String(c2.decrypt(cip), "ASCII")
    println(nm)

    cip = c1.encryptWithMyPub(msg.getBytes("ASCII"))
    println(cip.length)
    nm = new String(c1.decrypt(cip), "ASCII")
    println(nm)

  }

//  testMac()
  def testMac(): Unit = {
    println(KeyPack.localMachine)
    KeyPack.macInfo.foreach(println)
//   val local = InetAddress.getLocalHost
//    println(local)
//    /*
//    eth1  OK
//    wlan0 OK
//    wlan1
//    net0
//     */
//    println(NetworkInterface.getByName("eth1").getHardwareAddress)
    val ifs = NetworkInterface.getNetworkInterfaces
    while (ifs.hasMoreElements) {
      val nw = ifs.nextElement()
      println(nw + "     virtual?" + nw.isVirtual)
      val mac = nw.getHardwareAddress
      if (null != mac)
        Base64.getEncoder.encode(mac).map(_.asInstanceOf[Char]).foreach(print)
      println("\r\n------------------------------------------------------------------------------------")
    }
//
//    val nw = NetworkInterface.getByName("eth1")
//    val mac = Str.Utils.hexToStr(nw.getHardwareAddress)
//    println(mac)
//    println(new String(null.asInstanceOf[Array[Char]]))
  }

}
