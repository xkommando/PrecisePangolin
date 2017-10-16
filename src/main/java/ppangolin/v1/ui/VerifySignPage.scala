package ppangolin.v1.ui

import java.awt._
import java.awt.event.ActionEvent
import java.io.FileInputStream
import java.security.Signature
import javax.swing.JOptionPane.showMessageDialog
import javax.swing._

import net.liftweb.json._
import ppangolin.v1.Spec
import ppangolin.v1.misc.Str.Codec._
import ppangolin.v1.misc.Str.Utils._
import ppangolin.v1.sbox.HySBox
import ppangolin.v1.sbox.SUtils._
import ppangolin.v1.ui.Activities._
import ppangolin.v1.ui.misc.LoadingPage

/**
  *
  * verify string signature or file signature,
  * two functions are placed in one bigger panel
  * hiding/showing string input/file input to apply two kind of sign verification
  *
  * one file a time, does not support multi-instance
  * (this is not that necessary, since sign/verify sign/hash are IO intensive)
  *
  * Created by Bowen Cai on 11/6/2015.
  */
class VerifySignPage(win: MainView, hySBox: HySBox) extends JDialog(win) {

  val loadingDialog = new LoadingPage(this, "Verifying ")

  private var hashSpeed: Int = 0

  private val titleStrIn = "Verify String Signature"
  private val panelStrInput = new JPanel(new BorderLayout())
  private val combSignOps = new JComboBox[String]()
  private val txtStrIn = new JTextArea()
  private val undoMgrStrIn = undoRedoEnabled(txtStrIn)
  private val titleFileIn = "Verify File Signature"

  private val txtSign = new JTextArea()
  private val undoMgrSignIn = undoRedoEnabled(txtSign)
  private val radBtnB64 = new JRadioButton("Base 64")
  private val radBtnHex = new JRadioButton("Hex", true)
  private val btnPasteFromCB: JButton = new JButton("Paste")
  private val btnVerify = new JButton("Verify")

  def setupUI(): Unit = {
    setLocation(480, 250)
    setResizable(true)

    txtStrIn.setLineWrap(true)
    txtStrIn.setEditable(true)
    txtStrIn.setEnabled(true)

    txtSign.setLineWrap(true)
    txtSign.setEditable(true)
    txtSign.setEnabled(true)

    Spec.Signature.basicAlgo.foreach(combSignOps.addItem)
    Spec.Signature.extendedAlgo.foreach(combSignOps.addItem)

    val _pMain = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10))

    val _pnw = new JPanel()
    _pnw.setLayout(new BoxLayout(_pnw, BoxLayout.PAGE_AXIS))
    val _lbtxt = new JLabel("Text:")
    _lbtxt.setAlignmentX(Component.CENTER_ALIGNMENT)
    _pnw.add(_lbtxt)
    val _lbalgo = new JLabel("Algorithm:")
    _lbalgo.setAlignmentX(Component.CENTER_ALIGNMENT)
    _pnw.add(_lbalgo)
    val _pfill = new JPanel()
    _pfill.add(combSignOps)
    _pnw.add(_pfill)
    panelStrInput.setBorder(EtchedBorder)
    panelStrInput.add(_pnw, BorderLayout.WEST)
    panelStrInput.add(scrollEnabled(txtStrIn, 290, 150), BorderLayout.CENTER)
    _pMain.add(panelStrInput)

    val panelSign = new JPanel(new BorderLayout())
    val _psw = new JPanel()
    _psw.setLayout(new BoxLayout(_psw, BoxLayout.PAGE_AXIS))
    val _lbsign = new JLabel("Enter Signature:")
    _lbsign.setAlignmentX(Component.CENTER_ALIGNMENT)
    _psw.add(_lbsign)
    val prad = new JPanel()
    groupBtns(radBtnB64, radBtnHex)
    prad.add(radBtnB64)
    prad.add(radBtnHex)
    _psw.add(prad)
    panelSign.add(_psw, BorderLayout.WEST)
    panelSign.add(scrollEnabled(txtSign, 325, 130), BorderLayout.CENTER)
    _pMain.add(panelSign)

    val _ps = new JPanel()
    _ps.add(btnPasteFromCB)
    _ps.add(btnVerify)
    _pMain.add(_ps)

    add(_pMain)
  }

  val ops = Spec.Signature.basicAlgo
  // cache the algorithm name
  var vfileSignAlgo: String = _

  def start(): Unit = {
    setupUI()
    enableEditMenu(txtStrIn)
    enableEditMenu(txtSign)

    hashSpeed = (win.config \\ "file-hash" \ "bgtask-size-speed").asInstanceOf[JInt].values.toInt

    win.menuVeriSign.getSubElements()(0).asInstanceOf[JPopupMenu].getSubElements
      .foreach(_.asInstanceOf[JMenuItem].addActionListener((e: ActionEvent) => {
        txtSign.setText("")
      txtStrIn.setText("")
      undoMgrStrIn.discardAllEdits()
      undoMgrSignIn.discardAllEdits()
      radBtnHex.setSelected(true)
      setVisible(true)
    }))

    onClick(win.miVerifyStr, {
      setTitle(titleStrIn)
      panelStrInput.setVisible(true)
      setSize(540, 400)
    })

    win.menuVeriSign.getSubElements()(0).asInstanceOf[JPopupMenu].getSubElements // get all menu item
      .drop(1).foreach(_.asInstanceOf[JMenuItem].addActionListener((e: ActionEvent) => {
      val algo = e.getSource.asInstanceOf[JMenuItem].getText
      setTitle(s"$titleFileIn ($algo)")
      vfileSignAlgo = algo
      panelStrInput.setVisible(false)
      setSize(530, 255)
    }))

    onClick(btnVerify, {
      val strSign = txtSign.getText
      if (notBlank(strSign)) {
        var sgbs: Array[Byte] = null
        if (radBtnHex.isSelected && regexHexStr.matcher(strSign).matches())
          sgbs = fromHexStr(sanitizedHex(sanitizedHex(strSign)))
        else if (radBtnB64.isSelected && regexBase64Str.matcher(strSign).matches())
          sgbs = hySBox.B64Decoder.decode(sanitizedB64(strSign))
        else {
          showMessageDialog(win, "Unknown string encoding")
          return
        }

//        val sgbs = if (radBtnHex.isSelected) fromHexStr(sanitizedHex(sanitizedHex(strSign)))
//                      else hySBox.B64Decoder.decode(sanitizedB64(strSign))
        val algo = if (panelStrInput.isVisible) combSignOps.getSelectedItem.asInstanceOf[String]
                      else vfileSignAlgo
        try {
          if (getTitle == titleStrIn) {
            val strIn = txtStrIn.getText
            if (notBlank(strIn)) {
              val ds = Signature.getInstance(algo)
              ds.initVerify(hySBox.theirPub)
              ds.update(strIn.getBytes(ENCODING))
              val real = ds.verify(sgbs)
              if (real)
                showMessageDialog(win, "Authentic!")
              else showMessageDialog(win, "Fake!")
            }
          } else {
            val fc = new JFileChooser("Signed File")
            val ret = fc.showOpenDialog(win)
            if (ret == JFileChooser.FILES_ONLY) {
              val f = fc.getSelectedFile
              val fName = f.getPath
              val flen = f.length()
              var consumed = 0
              val estTime = flen / hashSpeed // 18M/S
              val work = ()=> {
                val ds = Signature.getInstance(algo)
                ds.initVerify(hySBox.theirPub)
                processStream(new FileInputStream(f), (buf, len) => {
                  ds.update(buf, 0, len)
                  consumed += len
                }).close()
                val real = ds.verify(sgbs)
                if (real)
                  showMessageDialog(win, "Authentic!")
                else showMessageDialog(win, "Fake!")
              }
              if (estTime > 3) {
                loadingDialog.setTitle("Verifying " + fName)
                loadingDialog.show(estTime.asInstanceOf[Int], () => {
                  consumed.asInstanceOf[Float] / flen
                })
                new SwingWorker[Unit, Unit] {
                  override def doInBackground(): Unit = work()
                  override def done(): Unit = loadingDialog.dismiss()
                }.execute()
              } else work()
            }
          }
        } catch {
          case e: Throwable => e.printStackTrace()
            showMessageDialog(win, "Could not verify signature" + exceptInfo(e))
        }
      }
    })

    onClick(btnPasteFromCB, {
      val txt = strFromClipBoard
      if (notBlank(txt))
        txtSign.append(txt)
    })
  }


}
