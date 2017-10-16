package ppangolin.v1.ui.misc

import java.awt.Frame
import javax.swing.JOptionPane._
import javax.swing._

import ppangolin.v1.misc.Str.Codec._
import ppangolin.v1.misc.Str.Utils._
import ppangolin.v1.sbox.HySBox
import ppangolin.v1.sbox.SUtils._
import ppangolin.v1.ui.Activities._

/**
  * show file hash result
  * used by HashFilePage and SignFilePage
  *
  * Created by Bowen Cai on 12/20/2015.
  */
class HashResultPage (title: String, hySBox: HySBox, parent:Frame, var hexEncoded: Boolean)
  extends JDialog(parent, title, false) {

  val btnBase64 = new JButton("Base64 Encoding")
  val lbLen = new JLabel("Length")
  val btnCopy = new JButton("Copy")
  val txtHash = new JTextArea()
  val btnCompare = new JButton("Compare Clipboard")

  private def setupUI(): Unit = {
    this.setLocation(480, 250)
    this.setSize(400, 250)
    this.setResizable(true)

    txtHash.setLineWrap(true)
    txtHash.setEditable(false)
    txtHash.setEnabled(true)
    val _p = new JPanel
    _p.add(lbLen)
    val _ptxt = new JPanel()
    _ptxt.setBorder(EmptyBorder(5, 8, 5, 8))
    _ptxt.add(scrollEnabled(txtHash, 370, 120))
    _p.add(_ptxt)

    _p.add(btnBase64)
    _p.add(btnCopy)
    _p.add(btnCompare)
    this.add(_p)
  }

  def start(): Unit = {
    setupUI()
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

    onClick(btnCopy, {
      toClipBoard(txtHash.getText)
      showMessageDialog(parent, "Hash copied")
    })
    onClick(btnBase64, {
      val hashStr = txtHash.getText
      if (notBlank(hashStr)) {
        if (hexEncoded) {
          txtHash.setText(hySBox.B64Encoder.encodeToString(fromHexStr(hashStr)))
          hexEncoded = false
          btnBase64.setText("Hex Encoding")
        } else {
          txtHash.setText(toHexStr(hySBox.B64Decoder.decode(hashStr.getBytes(ASCII))))
          hexEncoded = true
          btnBase64.setText("Base64 Encoding")
        }
      }
    })

    onClick(btnCompare, {
      val otherHash = strFromClipBoard
      if (notBlank(otherHash)) {
        val thisHash = txtHash.getText
        val fuzzyComp = false //(win.config \\ "fuzzy-compare").asInstanceOf[JBool]
        val same = if (fuzzyComp)
            ((hexEncoded && thisHash.equalsIgnoreCase(sanitizedHex(otherHash)))
              || thisHash == sanitizedB64(otherHash))
          else ((hexEncoded && thisHash.equalsIgnoreCase(otherHash))
            || thisHash == otherHash)
        if (same) showMessageDialog(parent, "Same Hash")
        else showMessageDialog(parent, "Not equal")
      } else
        showMessageDialog(parent, "Could not find string in clipboard")
    })
  }
}
