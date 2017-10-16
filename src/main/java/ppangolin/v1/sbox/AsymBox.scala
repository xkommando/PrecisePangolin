package ppangolin.v1.sbox

import java.security._
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import javax.crypto.Cipher

/**
 * Created by Bowen Cai on 9/23/2015.
 */
class AsymBox(val keySpec:String, val cipherName:String) extends SBox {

  val keyFactory = KeyFactory.getInstance(keySpec)

  private var _oPubK: PublicKey = _

  private var _mPubK: PublicKey = _
  private var _mPriK: PrivateKey = _

  def newCipher(): Cipher = Cipher.getInstance(cipherName)

  def setTheirPubKey(pubk: Array[Char]): Unit = setTheirPubKey(b64Decode(pubk))
  def setTheirPubKey(pubk: Array[Byte]): Unit = setTheirPubKey(keyFactory.generatePublic(new X509EncodedKeySpec(pubk)))

  def setTheirPubKey(pubk: PublicKey): Unit =  _oPubK = pubk

  def setMyKeyPair(pubk: Array[Byte], priK: Array[Byte]): Unit = {
    _mPriK = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(priK))
    _mPubK = keyFactory.generatePublic(new X509EncodedKeySpec(pubk))
  }

  def setMyKeyPair(pubk: Array[Char], priK: Array[Char]):Unit = setMyKeyPair(b64Decode(pubk), b64Decode(priK))

  def genMyKeyPair(len: Int = 2048): Unit = {
    val rsaKeyGen = KeyPairGenerator.getInstance(keySpec)
    rsaKeyGen.initialize(len)
    val kp = rsaKeyGen.genKeyPair()
    _mPriK = kp.getPrivate
    _mPubK = kp.getPublic
  }

  /**
    * @return char array of key, base 64 encoded
    */

  def theirPub: PublicKey = _oPubK
  def theirPubCA: Array[Char] = if (theirPub != null) b64Encode(theirPub.getEncoded) else null

  def myPub: PublicKey = _mPubK
  def myPubCA: Array[Char] = if (myPub != null) b64Encode(myPub.getEncoded) else null

  def myPri: PrivateKey = _mPriK
  def myPriCA: Array[Char] = if (myPri != null) b64Encode(myPri.getEncoded) else null


  def encryptWithMyPub(msg: Array[Byte]): Array[Byte] = {
    val c = newCipher()
    c.init(Cipher.ENCRYPT_MODE, myPub)
    c.doFinal(msg)
  }

  def encrypt(msg: Array[Byte]): Array[Byte] = {
    val c = newCipher()
    c.init(Cipher.ENCRYPT_MODE, theirPub)
    c.doFinal(msg)
  }

  def decrypt(msg: Array[Byte]): Array[Byte] = {
    val c = newCipher()
    c.init(Cipher.DECRYPT_MODE, myPri)
    c.doFinal(msg)
  }

  def destroy(): Unit = {
    //    if (_mPriK != null && !_mPriK.isDestroyed) _mPriK.destroy()
    destroyMyPair()
    destroyTheirPub()
  }

  def destroyMyPair(): Unit = {
    //    if (_mPriK != null && !_mPriK.isDestroyed) _mPriK.destroy()
    if (_mPriK != null) _mPriK = null//.destroy()
    _mPubK = null
  }

  def destroyTheirPub(): Unit = {
    _oPubK = null
  }
}

