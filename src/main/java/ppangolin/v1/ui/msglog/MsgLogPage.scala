package ppangolin.v1.ui.msglog

import java.awt.{BorderLayout, FlowLayout}
import javax.swing._

import ppangolin.v1.ui.Activities.{enableEditMenu, onClick, scrollEnabled}

/**
  *
  * right click on the log panel and show one piece of message log
  *
  * Created by Bowen Cai on 12/26/2015.
  */
class SinglePage extends JDialog {

  val txtUpper = new JTextArea
  val txtLower = new JTextArea
  val lbUpper = new JLabel()
  val lbLower = new JLabel()
  val btnOK = new JButton("OK")

  setLocation(450, 200)
  setSize(460, 380)
  setResizable(true)

//-----------------------------------------------------------------------------
  {
    setLayout(new BorderLayout())

    val pMain = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 12))
    txtUpper.setEditable(false)
    txtUpper.setEnabled(true)
    txtUpper.setLineWrap(true)
    pMain.add(lbUpper)
    pMain.add(scrollEnabled(txtUpper, 345, 130))

    txtLower.setEditable(true)
    txtLower.setEnabled(true)
    txtLower.setLineWrap(true)
    pMain.add(lbLower)
    enableEditMenu(txtLower)
    pMain.add(scrollEnabled(txtLower, 345, 130))
    pMain.add(btnOK)
    add(pMain, BorderLayout.CENTER)
    onClick(btnOK, {
      setVisible(false)
    })
  }

  def show(l: MsgLog): Unit = {
    val log = l.asInstanceOf[TextMsgLog]
    setTitle(log.toolTip)
    if (log.isEncrypt) {
      lbUpper.setText("Cipher Text:")
      txtUpper.setText(log.cipher)
      lbLower.setText("Plain Text:")
      txtLower.setText(log.plain)
    } else {
      lbUpper.setText("Plain Text:")
      txtUpper.setText(log.plain)
      lbLower.setText("Cipher Text:")
      txtLower.setText(log.cipher)
    }
    setVisible(true)
  }

}

class ListPage extends JDialog {

}