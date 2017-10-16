package ppangolin.v1.ui

import java.awt._
import javax.swing._

import ppangolin.v1.Spec
import ppangolin.v1.sbox.HySBox
import ppangolin.v1.ui.Activities._
import ppangolin.v1.ui.msglog.MsgLog

/**
  * Created by Bowen Cai on 11/22/2015.
  */
class MainView(sbox: HySBox, title:String) extends JFrame(title) {

  val miExit = new JMenuItem("Exit")
  val miExportKeys = new JMenuItem("Save Keys")
  val miLoadKeys = new JMenuItem("Load Keys")
  val miMyPair = new JMenuItem("My Key Pair")
  val miTheirPub = new JMenuItem("Their Pub Key")
  val miSecKey = new JMenuItem("Secrete Key")
  val menuEncrypt = new JMenu("Encrypt")
  val miEncryptFile = new JMenuItem("Encrypt File")
  val miDecryptFile = new JMenuItem("Decrypt File")
  val miEncryptFdr = new JMenuItem("Encrypt&Zip Folder")
  val miDecryptFdr = new JMenuItem("Unzip&Decrypt Folder")
  val miAbout = new JMenuItem("About")
  val miMailAuthor = new JMenuItem("Email Author")
  val cbMyPair = new JCheckBox("My Key Pair")
  val cbTheirPub = new JCheckBox("Their Pub Key")
  val cbSecKey = new JCheckBox("Secrete Key")
  val listLog = new JList[MsgLog]
  val logScroll = new JScrollPane(listLog)
  val btnClearTxt = new JButton("Clear")
  val menuHash = new JMenu("Hash File")
  val menuSign = new JMenu("Sign File")
  val menuVeriSign = new JMenu("Verify Sign")
  val miVerifyStr = new JMenuItem("Verify String")
  val txtOutput = new JTextArea
  val btnCopyToCB = new JButton("Copy")
  val txtInput = new JTextArea
  val btnPasteFromCB = new JButton("Paste")
  val btnEncrypt = new JButton("Encrypt")
  val btnDecrypt = new JButton("Decrypt")
  val radBtnB64 = new JRadioButton("Base 64", true)
  val radBtnHex = new JRadioButton("Hex")
  val btnHashB64 = new JButton("Hash")
  val combHash = new JComboBox[String]
  val btnSign = new JButton("Sign")
  val combSignatures = new JComboBox[String]

  val miImportMsgLog = new JMenuItem("Import Logs")
  val miExportMsgLog = new JMenuItem("Export Logs")
  val btnListLogs = new JButton("Display All")
  val btnClearLog = new JButton("Clear")
  val fmenuLog = new JPopupMenu()
  val fmiShowLog = new JMenuItem("Show")
  val fmiRevertLog = new JMenuItem("Revert")
  val fmiDelLog = new JMenuItem("Delete")

  /**
    * page = view + activity, so all views & pages shall joint together at main view
    * whereas activities does not.
    */
  var secKeyPage: SecKeyPage = null
  var keyPairPage: KeyPairPage = null
  var signFilePage: SignFilePage = null
  var verifySignPage: VerifySignPage = null

  var config: net.liftweb.json.JsonAST.JValue = null

  def start():Unit = {
    setupMainUI()
    enableEncrypt(false)
    enableSign(false)
    enableVeriSign(false)
  }

  private def setupMainUI():Unit = {
    this.setSize(640, 700)
    this.setLocation(400, 50)
    this.setResizable(true)
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    this.setLayout(new BorderLayout(5, 5))

    val menuBar = new JMenuBar()

    val menuFile = new JMenu("File")
    menuFile.add(miExportKeys)
    menuFile.add(miLoadKeys)
    menuFile.addSeparator()
    menuFile.add(miExportMsgLog)
    menuFile.add(miImportMsgLog)
    menuFile.addSeparator()
    menuFile.add(this.miExit)
    menuBar.add(menuFile)

    val menuKeys = new JMenu("Keys")
    menuKeys.add(miMyPair)
    menuKeys.add(miTheirPub)
    menuKeys.addSeparator()
    menuKeys.add(miSecKey)
    menuBar.add(menuKeys)

    menuEncrypt.add(miEncryptFile)
    menuEncrypt.add(miDecryptFile)
    menuEncrypt.addSeparator()
    menuEncrypt.add(miEncryptFdr)
    menuEncrypt.add(miDecryptFdr)
    menuBar.add(menuEncrypt)

    for (algo <- Spec.Hash.basicAlgo) {
      menuHash.add(new JMenuItem(algo))
      combHash.addItem(algo)
    }
    menuHash.addSeparator()
    for (algo <- Spec.Hash.extendedAlgo) {
      menuHash.add(new JMenuItem(algo))
      combHash.addItem(algo)
    }
    menuBar.add(menuHash)
    for (algo <- Spec.Signature.basicAlgo) {
      menuSign.add(new JMenuItem(algo))
      combSignatures.addItem(algo)
    }
    menuSign.addSeparator()
    for (algo <- Spec.Signature.extendedAlgo) {
      menuSign.add(new JMenuItem(algo))
      combSignatures.addItem(algo)
    }
    menuBar.add(menuSign)
    menuVeriSign.add(miVerifyStr)
    menuVeriSign.addSeparator()
    for (algo <- Spec.Signature.basicAlgo) {
      menuVeriSign.add(new JMenuItem(algo))
    }
    menuVeriSign.addSeparator()
    for (algo <- Spec.Signature.extendedAlgo) {
      menuVeriSign.add(new JMenuItem(algo))
    }
    menuBar.add(menuVeriSign)

    val menuHelp = new JMenu("Help")
    menuHelp.add(miMailAuthor)
    menuHelp.add(miAbout)
    menuBar.add(menuHelp)
    this.setJMenuBar(menuBar)

    val panelStatus = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5))
    panelStatus.setBorder(EtchedBorder)
    panelStatus.add(new JLabel("Keys:"))
    cbMyPair.setEnabled(false)
    panelStatus.add(cbMyPair)
    cbTheirPub.setEnabled(false)
    panelStatus.add(cbTheirPub)
    cbSecKey.setEnabled(false)
    panelStatus.add(cbSecKey)
    this.add(panelStatus, BorderLayout.NORTH)

    fmenuLog.add(fmiShowLog)
    fmenuLog.add(fmiRevertLog)
    fmenuLog.add(fmiDelLog)

    val panelLog = new JPanel(new BorderLayout(6, 0))
    panelLog.setBorder(BorderFactory.createTitledBorder("Log"))
    this.listLog.setBorder(BeveledBorder(Lowered))

    logScroll.setPreferredSize(new Dimension(190, 310))
    panelLog.add(logScroll, BorderLayout.CENTER)
    val _p: JPanel = new JPanel
    _p.add(btnListLogs)
    _p.add(btnClearLog)
    panelLog.add(_p, BorderLayout.SOUTH)
    this.add(panelLog, BorderLayout.WEST)

    val panelEdit = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 5))
    this.txtOutput.setBorder(CompoundBorder(EtchedBorder, EmptyBorder(2, 5, 2, 5)))
    this.txtOutput.setEditable(true)
    this.txtOutput.setEnabled(true)
    this.txtOutput.setLineWrap(true)
    panelEdit.add(new JLabel("Output:"))
    panelEdit.add(btnCopyToCB)
    val _marginP = new JPanel
    _marginP.setBorder(EmptyBorder(2, 0, 10, 0))
    _marginP.add(scrollEnabled(txtOutput, 400, 150))
    panelEdit.add(_marginP)

    txtInput.setBorder(CompoundBorder(BeveledBorder(Lowered), EmptyBorder(2, 5, 2, 5)))

    this.txtInput.setEditable(true)
    this.txtInput.setEnabled(true)
    this.txtInput.setLineWrap(true)
    this.txtInput.setRequestFocusEnabled(true)
    panelEdit.add(new JLabel("Input:"))
    panelEdit.add(btnPasteFromCB)
    panelEdit.add(scrollEnabled(txtInput, 400, 180))

    val _pb = new JPanel(new GridLayout(3, 2, 8, 12))
    panelEdit.add(btnEncrypt)
    panelEdit.add(btnDecrypt)
    panelEdit.add(btnClearTxt)

    groupBtns(radBtnB64, radBtnHex)
    _pb.add(radBtnB64)
    _pb.add(radBtnHex)
    _pb.add(combSignatures)
    _pb.add(btnSign)
    _pb.add(combHash)
    _pb.add(btnHashB64)
    panelEdit.add(_pb)

    this.add(panelEdit, BorderLayout.CENTER)
    this.setVisible(true)
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  }

  def enableEncrypt(ready: Boolean) {
    this.menuEncrypt.setEnabled(ready)
    this.btnEncrypt.setEnabled(ready)
    this.btnDecrypt.setEnabled(ready)
  }

  def enableSign(ready: Boolean) {
    this.menuSign.setEnabled(ready)
    this.combSignatures.setEnabled(ready)
    this.btnSign.setEnabled(ready)
  }

  def enableVeriSign(ready: Boolean) {
    this.menuVeriSign.setEnabled(ready)
  }

}

