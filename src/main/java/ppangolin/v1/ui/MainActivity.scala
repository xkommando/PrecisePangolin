package ppangolin.v1.ui

import java.awt.Desktop
import java.awt.event.ActionEvent
import java.io._
import java.net.URI
import java.security.{MessageDigest, Signature}
import java.util.Date
import javax.swing.JOptionPane._
import javax.swing._

import net.liftweb.json.JsonAST.JInt
import net.liftweb.json._
import ppangolin.v1._
import ppangolin.v1.misc.Str.Codec._
import ppangolin.v1.misc.Str.Utils._
import ppangolin.v1.sbox._
import ppangolin.v1.ui.Activities._
import ppangolin.v1.ui.msglog.{MsgLogger, TextMsgLog}

/**
  *
  * all components in main view, plus "help" and "file" menus
  *
  * Created by Bowen Cai on 11/4/2015.
  */
class MainActivity(win: MainView, hySBox: HySBox)  {

  val undoMgr = undoRedoEnabled(win.txtInput)

  var msgLogger : MsgLogger = null

  def start(): Unit = {
    win.enableVeriSign(true)

    setupMainMenus()
    enableEditMenu(win.txtInput)
    enableEditMenu(win.txtOutput)
    onClick(win.btnCopyToCB, {
      val txt = win.txtOutput.getText
      if (notBlank(txt))
        toClipBoard(txt)
    })

    onClick(win.btnPasteFromCB, {
      val txt = strFromClipBoard
      if(notBlank(txt))
        win.txtInput.append(txt)
    })

    onClick(win.btnEncrypt, {
      val txt = win.txtInput.getText
      if (notBlank(txt)) {
        val cip = try {
          val cipba = hySBox.encrypt(txt.getBytes(ENCODING))
          val cstr = new String(hySBox.B64Encoder.encode(cipba), ENCODING)
          msgLogger.prepend(TextMsgLog(isEncrypt = true, plain = txt, cipher = cstr))
          cstr
        } catch {
          case e: Throwable => e.printStackTrace()
            "Could not encrypt text" + exceptInfo(e)
        }
        win.txtOutput.setText(cip)
      }
    })

    onClick(win.btnDecrypt, {
      val txt = win.txtInput.getText
      if (notBlank(txt)) {
        val plainTxt = try {
          val plainbs = txt.getBytes(ENCODING)
          val compact = hySBox.B64Decoder.decode(plainbs)
          val pstr = new String(hySBox.decrypt(compact), ENCODING)
          msgLogger.prepend(TextMsgLog(isEncrypt = false, plain = pstr, cipher = txt))
          pstr
        } catch {
          case e: Throwable => e.printStackTrace()
            "Could not decrypt text" + exceptInfo(e)
        }
        win.txtOutput.setText(plainTxt)
      }
    })

    onClick(win.btnClearTxt, {
      win.txtInput.setText("")
      win.txtOutput.setText("")
      undoMgr.discardAllEdits()
    })

    onClick(win.btnHashB64, {
      val txt = win.txtInput.getText
      if (notBlank(txt)) {
        val algo = win.combHash.getSelectedItem.asInstanceOf[String]
        win.txtOutput.setText(try {
          val bs = MessageDigest.getInstance(algo).digest(txt.getBytes(ENCODING))
          if (win.radBtnB64.isSelected)
            hySBox.B64Encoder.encodeToString(bs)
          else
            toHexStr(bs)
        } catch {
          case e: Throwable => e.printStackTrace()
            "Could not hash input text using [" + algo + "]" + exceptInfo(e)
        })
      }
    })

    onClick(win.btnSign, {
      val txt = win.txtInput.getText
      if (notBlank(txt)) {
        val algo = win.combSignatures.getSelectedItem.asInstanceOf[String]
        win.txtOutput.setText(try {
          val ds = Signature.getInstance(algo)
          ds.initSign(hySBox.myPri)
          ds.update(txt.getBytes(ENCODING))
          val bs = ds.sign()
          if (win.radBtnB64.isSelected)
            hySBox.B64Encoder.encodeToString(bs)
          else
            toHexStr(bs)
        } catch {
          case e: Throwable => e.printStackTrace()
            "Could not sign using [" + algo + "]" + exceptInfo(e)
        })
      }
    })

  }

  private def setupMainMenus(): Unit = {
    onClick(win.miExportKeys, actSaveKeys)
    onClick(win.miLoadKeys, actLoadKeys)

    onClick(win.miExit, (e:ActionEvent) => {
      if ((hySBox.myPri != null || hySBox.theirPub != null)
        && 0 == showConfirmDialog(win, "Save Keys?")) {
        win.miExportKeys.doClick()
      }
      hySBox.destroy()
      win.dispose()
      //      System.exit(0)
    })

    onClick(win.miEncryptFile, actEncryptFile)
    onClick(win.miDecryptFile, actDecryptFile)

    onClick(win.miMailAuthor,
      try {
        Desktop.getDesktop.mail(new URI("mailto:feedback2bowen@outlook.com"))
      } catch {
        case e: Throwable => e.printStackTrace()
          showMessageDialog(win, "Could not open Email Client\r\n" + e.getMessage)
      })

    onClick(win.miAbout,
      showMessageDialog(win, "Author: Bowen Cai \r\nmailto:feedback2bowen@outlook.com\r\nCopyright 2015 Bowen Cai"
        , "Precise Pangolin version 0.88", INFORMATION_MESSAGE))
  }

  val actEncryptFile = (e: ActionEvent) => {
    val fcIn = new JFileChooser()
    fcIn.setDialogTitle("Select file to encrypt")
    fcIn.setDialogType(JFileChooser.OPEN_DIALOG)
    val ret = fcIn.showDialog(win, "Encrypt")
    if (ret == JFileChooser.FILES_ONLY) {
      val fplain = fcIn.getSelectedFile
      val fcOut = new JFileChooser("Select encrypted file")
      val retO = fcOut.showSaveDialog(win)
      if (retO == JFileChooser.FILES_ONLY) {
        var fcipher = fcOut.getSelectedFile
        var cipName = fcipher.getPath
        while (fcipher.exists()) {
          showMessageDialog(win, cipName + " Already exist")
          fcIn.showSaveDialog(win)
          fcipher = fcOut.getSelectedFile
          cipName = fcipher.getPath
        }
        try {
          finishCopy(in = new FileInputStream(fplain),
            out = hySBox.encryptStream(new FileOutputStream(fcipher)))
          showMessageDialog(win, "Encrypted to " + cipName)
        } catch {
          case e: Throwable => e.printStackTrace()
            showMessageDialog(win, s"Failed to encrypt [${fplain.getPath}]" + exceptInfo(e))
        }
      }
    }
  }

  val actDecryptFile = ( e: ActionEvent) => {
    val fc = new JFileChooser()
    fc.setDialogTitle("Select file to decrypt")
    fc.setDialogType(JFileChooser.OPEN_DIALOG)
    val ret = fc.showDialog(win, "Decrypt")
    if (ret == JFileChooser.FILES_ONLY) {
      val fcipher = fc.getSelectedFile
      val fcOut = new JFileChooser("Select decrypted file")
      val retO = fcOut.showSaveDialog(win)
      if (retO == JFileChooser.FILES_ONLY) {
        val fplain = fcOut.getSelectedFile
        try {
          finishCopy(in = hySBox.decryptStream(new FileInputStream(fcipher)),
            out = new FileOutputStream(fplain))
          showMessageDialog(win, "Decrypted to " + fcipher.getPath)
        } catch {
          case e: Throwable => e.printStackTrace()
            showMessageDialog(win, s"Failed to decrypt [${fcipher.getPath}" + exceptInfo(e))
        }
      }
    }
  }

  import net.liftweb.json.JsonDSL._

  private val jsonHead = (Spec.cfg \ "app").asInstanceOf[JObject].obj // List[JField]
  /**
    * json:
    * place config.json \ "app" as the first part of the json object
    * example:
    * <code>
    * {
    * "name":"Precise Pangolin",
    * "version":"1",
    * "time":"2015-11-12",
    * "debug":false,
    * "time":"Mon Dec 21 23:31:51 CST 2015",
    * "type":"KeyPack(Full)",
    * "value":{
    * "id":1450711911598,
    * "localMachine":{
    * ...
    * ...
    * }
    * </code>
    */

  val actSaveKeys = (e:ActionEvent)=> {
    if (hySBox.myPri == null && hySBox.theirPub == null) {
      showMessageDialog(win, "No key to be saved")
    } else {
      val ops = Array[AnyRef]("Public Only", "Full")
      val opIdx = showOptionDialog(win, "Save full pack or public pack only?",
        "Save key pack", 0, QUESTION_MESSAGE, null,
        ops, ops(0))
      val fc = new JFileChooser()
      fc.setDialogTitle("Save Keys To File")
      fc.setDialogType(JFileChooser.SAVE_DIALOG)
      val ret = fc.showDialog(win, "Save")
      if (ret == JFileChooser.FILES_ONLY) {
        val fk = fc.getSelectedFile
        try {
          val pack = if (opIdx == 0) // first option, public only
            KeyPack(hySBox.asybox)
          else
            KeyPack(hySBox)
          val saveType = "KeyPack(" + ops(opIdx) + ")"
          val json = "archive" ->
            (("time" -> new Date().toString) ~
              ("type" -> saveType) ~
              ("value" -> Extraction.decompose(pack)(DefaultFormats)))
          Printer.pretty(render(new JObject(jsonHead ++ json.obj)), new FileWriter(fk)).close()
//          Serialization.writePretty(pack, new FileWriter(fk))(DefaultFormats)
//            .close()
          showMessageDialog(win, ops(opIdx) + " Key pack saved to " + fk.getPath)
        } catch {
          case e: Throwable => e.printStackTrace()
            showMessageDialog(win,
              s"Could not save ${ops(opIdx)} key pack to [${fk.getPath}]" + exceptInfo(e))
        }
      }
    }
  }

  val actLoadKeys = (e:ActionEvent)=>{
    val fc = new JFileChooser()
    fc.setDialogTitle("Load Key File")
    fc.setDialogType(JFileChooser.OPEN_DIALOG)
    val ret = fc.showDialog(win, "Open")
    if (ret == JFileChooser.FILES_ONLY) {
      val fk = fc.getSelectedFile
      try {
        val jv = JsonParser.parse(new FileReader(fk), closeAutomatically = true).asInstanceOf[JObject] \ "archive" \ "value"
        val idN = jv \ "id"
        if (idN != net.liftweb.json.JNothing) {
          val id = idN.asInstanceOf[JInt].num.toLong
          if (id < 0) {
            // public key pack
            val pubPack = Extraction.extract(jv, TypeInfo(classOf[PublicPack], None))(DefaultFormats)
              .asInstanceOf[PublicPack]
            if (pubPack.theirPubKey.isDefined) {
              hySBox.setTheirPubKey(pubPack.theirPubKey.get.toCharArray)
              win.cbTheirPub.setSelected(true)
              win.cbTheirPub.setEnabled(true)
              showMessageDialog(win, "Add their public key")
            } else showMessageDialog(win, "Could not find their public key")
          } else {
            // full key pack
            val fp = Extraction.extract(jv, TypeInfo(classOf[FullKeyPack], None))(DefaultFormats)
              .asInstanceOf[FullKeyPack]
            hySBox.setMyKeyPair(fp.myPubKey.toCharArray, fp.myPrivKey.toCharArray)
            win.keyPairPage.showKeyPair(new String(fp.myPubKey), new String(fp.myPrivKey))
            val sb = new StringBuilder("Add my key pair")
            if (fp.theirPubKey.isDefined) {
              hySBox.setTheirPubKey(fp.theirPubKey.get.toCharArray)
              win.cbTheirPub.setSelected(true)
              win.cbTheirPub.setEnabled(true)
              win.enableVeriSign(true)
              sb ++= ", their public key"
            }
            if (fp.secreteKey.isDefined) {
              val sk = fp.secreteKey.get
              hySBox.setSecretKey(sk.toCharArray)
              sb ++= ", secrete key"
              win.secKeyPage.showSecKey(new String(sk))
              win.enableEncrypt(true)
            }
            showMessageDialog(win, sb.toString())
          }
        } else showMessageDialog(win, "Corrupted Key Pack")
      } catch {
        case e:Throwable => e.printStackTrace()
          showMessageDialog(win, "Could not parse file" + exceptInfo(e))
      }
    }
  }

}
