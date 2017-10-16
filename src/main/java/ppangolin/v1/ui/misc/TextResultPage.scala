package ppangolin.v1.ui.misc

import java.awt.Frame
import javax.swing.JOptionPane._
import javax.swing._

import ppangolin.v1.misc.Str.Utils._
import ppangolin.v1.ui.Activities._

/**
  * a small dialog with text area to show a small piece of text
  * used in right-click encoding/decoding
  *
  * Created by Bowen Cai on 12/23/2015.
  */
class TextResultPage (t: String, c:String, parent:Frame)
  extends JDialog(parent, t, false) {

  private val lbLen = new JLabel()
  private val btnCopy = new JButton("Copy")
  private val txtArea = new JTextArea()
  private val btnCompare = new JButton("Compare Clipboard")

  setContent(c)

  setupUI()
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

  onClick(btnCopy, {
    val str = txtArea.getText
    if (notBlank(str)) {
      toClipBoard(txtArea.getText)
      showMessageDialog(parent, "Text copied")
    } else showMessageDialog(parent, "Content is empty")
  })

  onClick(btnCompare, {
    val otherHash = strFromClipBoard
    if (notBlank(otherHash)) {
      if (txtArea.getText == otherHash) showMessageDialog(parent, "Same Text")
      else showMessageDialog(parent, "Not equal")
    } else
      showMessageDialog(parent, "Could not find string in clipboard")
  })

  def this(parent:Frame) {
    this(null, null, parent)
  }

  def setContent(c:String): TextResultPage = {
    txtArea.setText(c)
    lbLen.setText("Length: " + c.length)
    this
  }

  private def setupUI(): Unit = {
    this.setLocation(450, 250)
    this.setSize(300, 230)
    this.setResizable(true)

    txtArea.setLineWrap(true)
    txtArea.setEditable(true)
    txtArea.setEnabled(true)
    val _p = new JPanel
    _p.add(lbLen)
    val _ptxt = new JPanel()
    _ptxt.setBorder(EmptyBorder(3, 5, 3, 5))
    _ptxt.add(scrollEnabled(txtArea, 270, 100))
    _p.add(_ptxt)

    _p.add(btnCopy)
    _p.add(btnCompare)
    this.add(_p)
  }

}