package ppangolin.v1.sbox

import java.net.{InetAddress, NetworkInterface}
import java.util.Date

import ppangolin.v1.misc.Str

/**
 * to be serialized and stored in file
 * Created by Bowen Cai on 11/4/2015.
 */
sealed case class MacAddr(netName:String, mac:String)
sealed case class LocalMachineInfo(user:String, hostName:String, hostAddr:String, macInfo: Array[MacAddr])

object KeyPack {

  def apply(asymBox: AsymBox) =
    new PublicPack(-System.currentTimeMillis(), localMachine, new Date(),
      asymBox.keySpec,
      Option(new String(asymBox.myPubCA)),
      Option(new String(asymBox.theirPubCA)))


  def apply(hySBox: HySBox):FullKeyPack = {
//    val ks = List(hySBox.theirPubCA, hySBox.secretKeyCA).map(Option.apply)
//      .map(k=>k.map(new String(_)))
    new FullKeyPack(System.currentTimeMillis(), localMachine, new Date(),
      hySBox.asybox.keySpec,
      new String(hySBox.myPubCA), new String(hySBox.myPriCA), Option(new String(hySBox.theirPubCA)),
      hySBox.symbox.keySpec, Option(new String(hySBox.eSecretKeyCA)))
  }

  val userName = Some(System.getProperty("user.name")).getOrElse("UNK")
  val localHost = InetAddress.getLocalHost
  val macInfo = Array(
    "eth0", "eth1", "eth2", "eth3",
    "wlan0", "wlan1", "wlan2", "wlan3",
    "net0", "net1", "net2", "net3").map(name => {
    val net = try {
      NetworkInterface.getByName(name)
    } catch {
      case e:Throwable=> null.asInstanceOf[NetworkInterface]
    }
    if (net != null) {
      val mac = net.getHardwareAddress
      if (mac != null)
        MacAddr(name, Str.Codec.toHexStr(net.getHardwareAddress))
      else null
    } else null
  }).filter(_ != null)
  val localMachine = LocalMachineInfo(userName, localHost.getHostName, localHost.getHostAddress, macInfo)
}

case class PublicPack(id: Long, localMachine:LocalMachineInfo,
                             timestamp: Date,
                              keySpec: String,
                             myPubKey: Option[String],
                             theirPubKey: Option[String])

case class FullKeyPack(id: Long,
                       localMachine:LocalMachineInfo,
                       timestamp: Date,
                       asyKeySpec: String,
                       myPubKey: String,
                       myPrivKey: String,
                       theirPubKey: Option[String],
                       secKeySpec: String,
                       secreteKey: Option[String])
