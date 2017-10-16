package ppangolin.v1.ui.msglog

import java.awt.event.{ActionEvent, MouseAdapter, MouseEvent}
import java.io.{FileReader, FileWriter}
import java.util.Date
import javax.swing.JFileChooser
import javax.swing.JOptionPane._

import net.liftweb.json._
import ppangolin.v1._
import ppangolin.v1.ui.Activities._
import ppangolin.v1.ui.{MainActivity, MainView}

import scala.util.Random

/**
  *
  * control message log on the left part of the main view
  *
  * Created by Bowen Cai on 12/21/2015.
  */
class MsgLoggerActivity(win: MainView, main: MainActivity) {

  val msgLogger = new MsgLogger
  val encryptPage = new SinglePage

  val logScrollBar = win.logScroll.getVerticalScrollBar()
  var logIdx = 0

  def start(): Unit = {
    ///////////////////////////////////////////////////////////
    val tr = new Random
    msgLogger ++= (for (s <- Spec.Hash.basicAlgo) yield TextMsgLog(tr.nextBoolean(), s, s))
    msgLogger.prepend(TextMsgLog(tr.nextBoolean(), "一二三四五六七八九十一二三四五六七八九十", "这是密文"))
    ///////////////////////////////////////////////////////////

    win.listLog.setModel(msgLogger)
    win.listLog.setCellRenderer(new LogCellRenderer())

    /**
      * why not add tool tip to each label (message log)?
      * tool tip on cell could not always be displayed in the right position (too low)
      */
    win.listLog.addMouseMotionListener(new MouseAdapter {
      override def mouseMoved(e: MouseEvent): Unit = {
        val idx = win.listLog.locationToIndex(e.getPoint)
        if (idx >= 0) {
          val log = msgLogger.getElementAt(idx)
          win.listLog.setToolTipText(log.toolTip)
        }
      }
    })

    onMousePress(win.listLog, {
      case Right(e) =>
        logIdx = win.listLog.locationToIndex(e.getPoint)
        if (logIdx >= 0) {
          win.listLog.setSelectedIndex(logIdx)
          val bs = win.listLog.getCellBounds(logIdx, logIdx)
          //          println(logIdx + "  " + bs.getY + "   " + bar.getValue())
          // when scroll down, cut the scroll down offset
          win.fmenuLog.show(win, 120, 118 + bs.getY.asInstanceOf[Int] - logScrollBar.getValue())
        }
      case Left(e) =>
    })

    onClick(win.fmiShowLog, {
      val log = msgLogger.getElementAt(logIdx)
      encryptPage.show(log)
    })
    onClick(win.fmiRevertLog, {
      val log = msgLogger.getElementAt(logIdx).asInstanceOf[TextMsgLog]
      if (log.isEncrypt) {
        win.txtInput.setText(log.plain)
        win.txtOutput.setText(log.cipher)
      } else {
        win.txtInput.setText(log.cipher)
        win.txtOutput.setText(log.plain)
      }
      main.undoMgr.discardAllEdits()
    })
    onClick(win.fmiDelLog, {
      msgLogger.remove(logIdx)
    })

    onClick(win.btnClearLog, {
      if (YES_OPTION == showConfirmDialog(win, "Remove all logs?"))
        msgLogger.clear()
    })

    onClick(win.miExportMsgLog, actArchMsgLog)
    onClick(win.miImportMsgLog, actLoadMsgLog)
  }

  import net.liftweb.json.JsonDSL._

  private val jsonHead = (Spec.cfg \ "app").asInstanceOf[JObject].obj // List[JField]
  /**
    * json:
    * place config.json \ "app" as the first part of the json object
    * example:
    * <code>
*{
  *"name":"Precise Pangolin",
  *"version":"1",
  *"time":"2015-11-12",
  *"debug":false,
  *"archive":{
    *"time":"Mon Dec 21 23:41:16 CST 2015",
    *"type":"msglog",
    *"value":[{
      *"isEncrypt":true,
      *"plain":"
    *...
*...
*}
    * </code>
    */

  val actArchMsgLog = (e:ActionEvent) => {
    if (msgLogger.getSize() <= 0) {
      showMessageDialog(win, "No logs to be saved")
    } else {
      val fc = new JFileChooser()
      fc.setDialogTitle("Message Logs To File")
      fc.setDialogType(JFileChooser.SAVE_DIALOG)
      val ret = fc.showDialog(win, "Save")
      if (ret == JFileChooser.FILES_ONLY) {
        val fk = fc.getSelectedFile
        val logs = msgLogger.getLogArray
        try {
          val json ="archive" ->
            (("time" -> new Date().toString) ~
              ("type" -> "msglog") ~
              ("value" -> Extraction.decompose(logs)(DefaultFormats)))
          Printer.pretty(render(jsonHead ++ json.obj), new FileWriter(fk)).close()
          showMessageDialog(win, logs.length + " message logs saved to " + fk.getPath)
        } catch {
          case e: Throwable => e.printStackTrace()
            showMessageDialog(win,
              s"Could not save ${logs.length} message logs to [${fk.getPath}]" + exceptInfo(e))
        }
      }
    }
  }

  val actLoadMsgLog = (e:ActionEvent) => {
    val fc = new JFileChooser()
    fc.setDialogTitle("Load Message Logs")
    fc.setDialogType(JFileChooser.OPEN_DIALOG)
    val ret = fc.showDialog(win, "Open")
    if (ret == JFileChooser.FILES_ONLY) {
      val fk = fc.getSelectedFile
      try {
        val nlgs = (JsonParser.parse(new FileReader(fk), closeAutomatically = true)
          .asInstanceOf[JObject] \ "archive" \ "value").asInstanceOf[JArray].arr
          .map(Extraction.extract(_, TypeInfo(classOf[TextMsgLog], None))(DefaultFormats).asInstanceOf[TextMsgLog])
        if (nlgs.isEmpty)
          showMessageDialog(win, "Empty log file")
        else if (msgLogger.nonEmpty) {
          showConfirmDialog(win, "Merge " + nlgs.length + " logs into current panel (click 'No' to replace current logs) ?") match {
            case YES_OPTION =>
              msgLogger.addAll(nlgs)
              showMessageDialog(win, nlgs.length + " messages loaded and merged")
            case NO_OPTION =>
              msgLogger.clear()
              nlgs.foreach(msgLogger.insert)
              showMessageDialog(win, nlgs.length + " messages loaded")
            case _=>
          }
        }
        else {
          nlgs.foreach(msgLogger.insert)
          showMessageDialog(win, nlgs.length + " messages loaded ")
        }
      } catch {
        case e:Throwable => e.printStackTrace()
          showMessageDialog(win, "Could not parse file" + exceptInfo(e))
      }
    }
  }
}
