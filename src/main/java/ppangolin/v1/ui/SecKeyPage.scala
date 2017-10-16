package ppangolin.v1.ui

import java.awt.event.{ActionEvent, ItemEvent}
import javax.swing.JOptionPane.showMessageDialog
import javax.swing._

import ppangolin.v1.misc.Str.Utils._
import ppangolin.v1.sbox.HySBox
import ppangolin.v1.sbox.SUtils._
import ppangolin.v1.ui.Activities._

/**
 * user can enter secrete key (base-64 encoding)
 *
 * Created by Bowen Cai on 11/6/2015.
 */
class SecKeyPage(win: MainView, hySBox: HySBox) extends JDialog(win, "Secrete Key", true) {

  private val btnAutoGen  = new JButton("Auto Generate")
  private val lbSec = new JLabel("Secrete Key")
  private val btnOK  = new JButton("OK")

  private val txtSecKey = new JTextArea
//  var userEdit = false // content edited by program
  private val undoMrg = undoRedoEnabled(txtSecKey)

  def setupUI(): Unit = {
    this.setLocation(480, 250)
    this.setSize(360, 240)
    this.setResizable(false)
    txtSecKey.setLineWrap(true)
    txtSecKey.setEditable(true)
    txtSecKey.setEnabled(true)
    this.add(new JLabel("Secrete Key"))
    val _p = new JPanel
    _p.add(lbSec)
    _p.add(scrollEnabled(txtSecKey, 280, 120))
    _p.add(btnAutoGen)
    _p.add(btnOK)
    this.add(_p)
  }

//  private val keyLengthOps = Array[AnyRef]("128", "192", "256")
  private val keyLengthOps = Array[AnyRef](Int.box(128), Int.box(192), Int.box(256))

  def start(): Unit = {
    setupUI()
    enableEditMenu(txtSecKey)
    onClick(win.miSecKey, (e: ActionEvent) =>
      if (hySBox.theirPub != null) {
        val lb = lbSec
        if (win.cbSecKey.isSelected) {
          lb.setText("Secrete Key: " + txtSecKey.getText.length)
        } else lb.setText("Secrete Key")
        this.setVisible(true)
      } else {
        showMessageDialog(win, "Please Enter their public key first")
        win.miTheirPub.doClick()
      })

    onClick(btnAutoGen, {
      val len = JOptionPane.showInputDialog(win,
        "Select key length", "Generate Secrete Key",
        JOptionPane.QUESTION_MESSAGE, null,
        keyLengthOps, keyLengthOps(0))
      if (len != null) try {
          hySBox.genSecretKey(len.asInstanceOf[Int])
          showSecKey(hySBox.B64Encoder.encodeToString(hySBox.eSecretKey))
        } catch {
          case e: Throwable => e.printStackTrace()
            showMessageDialog(win, "Could not generate key" + exceptInfo(e))
        }
    })

    onToggle(win.cbSecKey, (e: ItemEvent) =>
      if (e.getStateChange == ItemEvent.DESELECTED) {
        hySBox.destroySecrete()
        txtSecKey.setText("")
        win.cbSecKey.setEnabled(false)
        win.enableEncrypt(false)
      })

//    txtSecKey.getDocument.addDocumentListener(new DocumentListener {
//      override def insertUpdate(e: DocumentEvent): Unit = userEdit = true
//      override def changedUpdate(e: DocumentEvent): Unit = userEdit = true
//      override def removeUpdate(e: DocumentEvent): Unit = userEdit = true
//    })
    /**
      * detecting user entered secrete key by checking undoMrg
      */
    onClick(btnOK, {
      if (undoMrg.canUndoOrRedo) { // content changed by user
        val txt = txtSecKey.getText
        if (notBlank(txt)) try {
            val sk = hySBox.B64Decoder.decode(sanitizedB64(txt))
            hySBox.setSecretKey(sk)
            showMessageDialog(win, "Change Applied")
            win.enableEncrypt(true)
          } catch {
            case e:Throwable => e.printStackTrace()
              showMessageDialog(win, "Could not apply change" + exceptInfo(e))
              txtSecKey.setText("")
          }
        else showMessageDialog(win, "Change discarded")
      }
      this.setVisible(false)
    })
  }

  def showSecKey(sk:String): Unit = {
//    userEdit = false // content edited by program
    undoMrg.discardAllEdits()
    txtSecKey.setText(sk)
    lbSec.setText("Length: " + (sk.length * 6)) // base 64 encode
    win.cbSecKey.setSelected(true)
    win.cbSecKey.setEnabled(true)
  }
}
