package ppangolin.v1.ui.misc

import java.awt.event.ActionEvent
import java.util.regex.Pattern
import javax.swing.JOptionPane.showMessageDialog
import javax.swing.text.TextAction

import ppangolin.v1.misc.Base64
import ppangolin.v1.misc.Str.Codec._
import ppangolin.v1.misc.Str.Utils.notBlank
import ppangolin.v1.sbox.SUtils._
import ppangolin.v1.ui.Activities._

/**
  * pop up menu for main view input text
  * when select text and right click it should pop up
  *
  * see Activities enableEditMenu
  *
  * Created by Bowen Cai on 12/21/2015.
  */
class CodecMenuAction(op: Int, label: String) extends TextAction(label) {
  override def actionPerformed(e: ActionEvent): Unit = {
    val str = getTextComponent(e).getSelectedText

    def encode(doproc: Array[Byte] => String): Unit =
      try {
        val estr = doproc(str.getBytes(ENCODING))
        new TextResultPage(label, estr, null).setVisible(true)
      } catch {
        case t: Throwable => t.printStackTrace()
          showMessageDialog(null, s"Could not encode [$str] with $label")
      }

    def decode(rgx: Pattern, doproc: String => Array[Byte]): Unit =
      if (rgx.matcher(str).matches()) {
        try {
          val estr = new String(doproc(str), ENCODING)
          new TextResultPage(label, estr, null).setVisible(true)
        } catch {
          case t: Throwable => t.printStackTrace()
            showMessageDialog(null, s"Could not decode [$str] with $label")
        }
      } else showMessageDialog(null, "[" + str + "] is not a " + label + " string")

    if (notBlank(str)) {
      op match {
        case 1 => // encode b64:  str -> base 64 char
          encode(Base64.getEncoder.encodeToString)
        case 2 => // decode b64: base 64 char -> str
          decode(regexBase64Str, Base64.getDecoder.decode)
        case 3 => // encode hex
          encode(toHexStr)
        case 4 => // decode hex
          decode(regexHexStr, fromHexStr)
        case 5 => // encode binary
          encode(toBinStr)
        case 6 => // decode bin
          decode(regexBinStr, fromBinStr)
      }
    }
  }
}
