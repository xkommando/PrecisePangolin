package ppangolin.v1.sbox

import java.io.{InputStream, OutputStream}
import javax.crypto._
import javax.crypto.spec.SecretKeySpec

/**
 * Created by Bowen Cai on 11/3/2015.
 */
class SymBox(val keySpec:String, val cipherName:String) extends SBox {

  def newCipher(): Cipher = Cipher.getInstance(cipherName)
  //  def newCipher(): Cipher = Cipher.getInstance("AES/CFB8/NoPadding")

  private var _key: SecretKey = _

  def setSecretKey(k: SecretKey):Unit = _key = k
  def setSecretKey(bk: Array[Byte]):Unit = setSecretKey(new SecretKeySpec(bk, keySpec))
  def setSecretKey(pubk: Array[Char]):Unit = setSecretKey(b64Decode(pubk))

  def secretKey =  _key
  def secretKeyCA: Array[Char] = if (secretKey != null) b64Encode(secretKey.getEncoded) else null

  /**
    * @param len must be equal to 128, 192 or 256
    */
  def genSecretKey(len: Int): Unit = {
    val keyGen = KeyGenerator.getInstance(keySpec)
    keyGen.init(len)
    _key = keyGen.generateKey()
  }

  def encrypt(msg: Array[Byte]): Array[Byte] = {
    val c = newCipher()
    c.init(Cipher.ENCRYPT_MODE, secretKey)
    c.doFinal(msg)
  }

  def decrypt(msg: Array[Byte]): Array[Byte] = {
    val c = newCipher()
    c.init(Cipher.DECRYPT_MODE, secretKey)
    c.doFinal(msg)
  }

  def encryptStream(out: OutputStream): CipherOutputStream = {
    val c = newCipher()
    c.init(Cipher.ENCRYPT_MODE, secretKey)
    new CipherOutputStream(out, c)
  }

  def decryptStream(in: InputStream): CipherInputStream = {
    val c = newCipher()
    c.init(Cipher.DECRYPT_MODE, secretKey)
    new CipherInputStream(in, c)
  }

  def destroy(): Unit = {
    //    if (_key != null && !_key.isDestroyed)
    if (_key != null)
      _key = null
    //        _key.destroy()
  }

}
