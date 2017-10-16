package ppangolin.v1.sbox

import javax.crypto.Cipher

import ppangolin.v1.misc.Base64

/**
 * Created by Bowen Cai on 11/3/2015.
 */
trait SBox {

  val B64Encoder = Base64.getEncoder
  val B64Decoder = Base64.getDecoder

  def newCipher(): Cipher

  def encrypt(msg: Array[Byte]): Array[Byte]
  def decrypt(msg: Array[Byte]): Array[Byte]

  def destroy()

  protected def b64Encode(bs:Array[Byte]):Array[Char] = B64Encoder.encode(bs).map(_.asInstanceOf[Char])
  protected def b64Decode(bs:Array[Char]):Array[Byte] = B64Decoder.decode(bs.map(_.asInstanceOf[Byte]))

}
