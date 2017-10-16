package ppangolin.v1

import java.io.InputStreamReader

import net.liftweb.json.JsonAST.{JArray, JObject, JValue}
import net.liftweb.json.{JString, JsonParser}

/**
 * Created by Bowen Cai on 11/13/2015.
 */
object Spec {

  val cfg = JsonParser.parse(new InputStreamReader(this.getClass.getClassLoader
    .getResourceAsStream("config.json")))
    .asInstanceOf[JObject]

  val ENCODING = (cfg \ "app" \ "encoding").asInstanceOf[JString].values

  def strArr(jv: JValue): Array[String] = jv.asInstanceOf[JArray].arr.map(_.asInstanceOf[JString].values).toArray

  object Hash {
    private val hash = cfg \ "sepc" \ "hash"
    val basicAlgo = strArr(hash \ "basic")
    val extendedAlgo = strArr(hash \ "extended")
  }

  object Signature {
    private val signature = cfg \ "sepc" \ "signature"
    val basicAlgo = strArr(signature \ "basic")
    val extendedAlgo = strArr(signature \ "extended")
  }

//  object Probe {

    import scala.collection.convert.wrapAsScala._

    private def printSet(setName: String, algorithms: java.util.Set[String]) {
      System.out.println(setName + ":")
      if (algorithms.isEmpty)
        System.out.println("            None available.")
      else for (algorithm <- algorithms)
          System.out.println("            " + algorithm)
    }

//    def main(args: Array[String]) {
//      Security.addProvider(new BouncyCastleProvider)
//      val providers = Security.getProviders
//      val ciphers = new util.TreeSet[String]
//      val keyAgreements = new util.TreeSet[String]
//      val macs = new util.TreeSet[String]
//      val messageDigests = new util.TreeSet[String]
//      val signatures = new util.TreeSet[String]
//      val keyFactory = new util.TreeSet[String]
//      val keyPairGenerator = new util.TreeSet[String]
//      val keyGenerator = new util.TreeSet[String]
//
//      providers.foreach(_.keySet().foreach(_e=>{
//        var entry = _e.asInstanceOf[String]
//
//          if (entry.startsWith("Alg.Alias.")) {
//            entry = entry.substring("Alg.Alias.".length)
//          }
//          if (entry.startsWith("Cipher.")) {
//            ciphers.add(entry.substring("Cipher.".length))
//          }
//          else if (entry.startsWith("KeyAgreement.")) {
//            keyAgreements.add(entry.substring("KeyAgreement.".length))
//          }
//          else if (entry.startsWith("Mac.")) {
//            macs.add(entry.substring("Mac.".length))
//          }
//          else if (entry.startsWith("MessageDigest.")) {
//            messageDigests.add(entry.substring("MessageDigest.".length))
//          }
//          else if (entry.startsWith("Signature.")) {
//            signatures.add(entry.substring("Signature.".length))
//          }
//          else if (entry.startsWith("KeyPairGenerator.")) {
//            keyPairGenerator.add(entry.substring("KeyPairGenerator.".length))
//          }
//          else if (entry.startsWith("KeyFactory.")) {
//            keyFactory.add(entry.substring("KeyFactory.".length))
//          }
//          else if (entry.startsWith("KeyGenerator.")) {
//            keyGenerator.add(entry.substring("KeyGenerator.".length))
//          }
//          else {
//            System.out.println(entry)
//          }
//
//      }))
//
//      printSet("KeyGenerator", keyGenerator)
//      printSet("KeyFactory", keyFactory)
//      printSet("KeyPairGenerator", keyPairGenerator)
//      printSet("Ciphers", ciphers)
//      printSet("KeyAgreeents", keyAgreements)
//      printSet("Macs", macs)
//      printSet("MessageDigests", messageDigests)
//      printSet("Signatures", signatures)
//  }
}
