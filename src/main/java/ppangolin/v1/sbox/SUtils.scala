package ppangolin.v1.sbox
import java.util.regex.Pattern

/**
 * Created by Bowen Cai on 11/3/2015.
 */
object SUtils {

  val regexBinStr = Pattern.compile("[0|1]*")
  // filter input
  val regexNonHexChar = Pattern.compile("[^a-fA-F0-9]")
  // filter input
  val regexHexStr = Pattern.compile("[a-fA-F0-9]*")

  // filter input
  @inline
  def sanitizedHex(in: String): String = regexNonHexChar.matcher(in).replaceAll("")

  val regexNonBase64Char = Pattern.compile("[^a-zA-Z0-9+/=]")
  // filter input
  val regexBase64Str = Pattern.compile("[a-zA-Z0-9+/=]*")

  // filter input
  @inline
  def sanitizedB64(in: String): String = regexNonBase64Char.matcher(in).replaceAll("")
}
