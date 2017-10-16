package ppangolin.v1.sbox

import java.io.{InputStream, OutputStream}
import java.security.{PrivateKey, PublicKey}
import javax.crypto.{Cipher, CipherInputStream, CipherOutputStream, SecretKey}

/**
 * Created by Bowen Cai on 11/3/2015.
 */
class HySBox(val asybox: AsymBox, val symbox: SymBox) extends SBox {

  def setTheirPubKey(pubk: PublicKey) = asybox.setTheirPubKey(pubk)
  def setTheirPubKey(pubk: Array[Byte]) = asybox.setTheirPubKey(pubk)
  def setTheirPubKey(pubk: Array[Char]) = asybox.setTheirPubKey(pubk)

  def setMyKeyPair(pubk: Array[Byte], priK: Array[Byte]) = asybox.setMyKeyPair(pubk, priK)
  def setMyKeyPair(pubk: Array[Char], priK: Array[Char]) = asybox.setMyKeyPair(pubk, priK)

  def genMyKeyPair(len: Int = 2048) = asybox.genMyKeyPair(len)

  def theirPub: PublicKey = asybox.theirPub
  def theirPubCA: Array[Char] = asybox.theirPubCA
  def myPubCA: Array[Char] = asybox.myPubCA
  def myPub: PublicKey = asybox.myPub
  def myPriCA: Array[Char] = asybox.myPriCA
  def myPri: PrivateKey = asybox.myPri

  def genSecretKey(len: Int) = symbox.genSecretKey(len)

  def newCipher(): Cipher = ???

  def eSecretKey: Array[Byte] = {
    val sk = symbox.secretKey
    if (sk == null) null
    else asybox.encrypt(sk.getEncoded)
  }

  def secretKey = symbox.secretKey

  def eSecretKeyCA: Array[Char] = {
    val sk = eSecretKey
    if (sk == null) null
    else b64Encode(sk)
  }

  def setSecretKey(sk: SecretKey): Unit = symbox.setSecretKey(sk)
  def setSecretKey(cip: Array[Byte]): Unit = symbox.setSecretKey(asybox.decrypt(cip))
  def setSecretKey(cip: Array[Char]): Unit = setSecretKey(b64Decode(cip))

  def encrypt(msg: Array[Byte]): Array[Byte] = symbox.encrypt(msg)
  def decrypt(msg: Array[Byte]): Array[Byte] = symbox.decrypt(msg)
  def encryptWithMyPub(msg: Array[Byte]): Array[Byte] = asybox.encryptWithMyPub(msg)
  def encryptWithSecKey(msg: Array[Byte]): Array[Byte] = symbox.encrypt(msg)

  def encryptStream(out: OutputStream): CipherOutputStream = symbox.encryptStream(out)
  def decryptStream(in: InputStream): CipherInputStream = symbox.decryptStream(in)

  def destroyMyPair(): Unit = asybox.destroyMyPair()
  def destroyTheirPub(): Unit = asybox.destroyTheirPub()
  def destroySecrete(): Unit = symbox.destroy()

  def destroy(): Unit = {
    asybox.destroy()
    symbox.destroy()
  }
}
