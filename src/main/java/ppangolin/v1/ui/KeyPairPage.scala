package ppangolin.v1.ui

import java.awt.event.ItemEvent
import javax.swing.JOptionPane.showMessageDialog
import javax.swing._

import ppangolin.v1.misc.Str.Utils._
import ppangolin.v1.sbox.HySBox
import ppangolin.v1.ui.Activities._


/**
  *
  *  user cannot enter key pair, key pair can only be generated automatically (or import from file)
  *
  * Created by Bowen Cai on 11/5/2015.
  */
class KeyPairPage(win:MainView, hySBox: HySBox) extends JDialog(win, "My Key Pair", true) {

  private val lbPub = new JLabel("Public Key:")
  private val txtPub = new JTextArea
  private val txtPriv = new JTextArea
  private val btnAutoGen = new JButton("Auto Generate")
  private val btnCopy2CB = new JButton("Copy Pub Key")
  private val btnOK = new JButton("OK")

  private def setupUI(): Unit = {

    this.setLocation(500, 250)
    this.setSize(380, 430)
    this.setResizable(false)

    val pMain = new JPanel
    txtPub.setEditable(true)
    txtPub.setEnabled(true)
    txtPub.setLineWrap(true)
    pMain.add(lbPub)
    pMain.add(scrollEnabled(txtPub, 340, 100))

    txtPriv.setEditable(true)
    txtPriv.setEnabled(true)
    txtPriv.setLineWrap(true)
    pMain.add(new JLabel("Private Key"))
    pMain.add(scrollEnabled(txtPriv, 340, 175))
    val pBtn: JPanel = new JPanel
    pBtn.setBorder(EmptyBorder(10, 0, 0, 0))
    pBtn.add(btnAutoGen)
    pBtn.add(btnCopy2CB)
    pBtn.add(btnOK)
    pMain.add(pBtn)
    this.add(pMain)
  }

  private val ops = Array[AnyRef]("Copy Public Key", "OK")

  def start(): Unit = {
    setupUI()

    onClick(win.miMyPair, this.setVisible(true))

    onClick(btnAutoGen, {
      var in: String = null
      while (!isDigits(in))
        in = JOptionPane.showInputDialog(win, "Enter Length(>=512)", "Key Pair Length", JOptionPane.QUESTION_MESSAGE)
      try {
        val len = Integer.parseInt(in)
        hySBox.genMyKeyPair(len)
        showKeyPair(new String(hySBox.myPubCA), new String(hySBox.myPriCA))
        val opIdx = JOptionPane.showOptionDialog(win, "Key pair generated, length:",
          "Auto Generate Key Pair", 0, JOptionPane.QUESTION_MESSAGE, null,
          ops, ops(0))
        if (opIdx == 0) {
          toClipBoard(new String(hySBox.myPubCA))
          showMessageDialog(win, "Public Key Copied")
        }
        this.setVisible(false)
      } catch {
        case e: Throwable => e.printStackTrace()
          showMessageDialog(win, "Could not generate key pair" + exceptInfo(e))
      }
    })

    /**
      * if un-checks the box button, delete key pair
      */
    onToggle(win.cbMyPair, (e: ItemEvent) =>
      if (e.getStateChange == ItemEvent.DESELECTED) {
        hySBox.destroyMyPair()
        txtPriv.setText("")
        txtPub.setText("")
        win.cbMyPair.setEnabled(false)
        win.enableSign(false)
      })

    onClick(win.miTheirPub, {
      val in = JOptionPane.showInputDialog(win, "Enter Public Key", "Their Public Key", JOptionPane.QUESTION_MESSAGE)
      if (notBlank(in)) try {
        hySBox.setTheirPubKey(in.toCharArray)
        win.cbTheirPub.setSelected(true)
        win.cbTheirPub.setEnabled(true)
        win.enableVeriSign(true)
      } catch {
        case e:Throwable => e.printStackTrace()
          showMessageDialog(win, "Could not set public key" + exceptInfo(e))
      }
    })

    onToggle(win.cbTheirPub, (e:ItemEvent)=>{
      val op = e.getStateChange  // on == 1  off == 2
      if (op == 2) {
        hySBox.destroyTheirPub()
        win.cbTheirPub.setEnabled(false)
        win.menuVeriSign.setEnabled(false)
      }
    })

    /**
      *  user cannot enter key pair, key pair can only be generated automatically (or import from file)
      *  ok button does nothing but hide the dialog
      */
    onClick(btnOK, this.setVisible(false))

    onClick(btnCopy2CB, toClipBoard(new String(hySBox.myPubCA)))
  }

  def showKeyPair(pub:String, privK:String): Unit = {
    lbPub.setText("Public Key: " + (pub.length * 6)) // base 64 encoding
    txtPub.setText(pub)
    txtPriv.setText(privK)
    win.cbMyPair.setSelected(true)
    win.cbMyPair.setEnabled(true)
    win.enableSign(true)
  }
}
