package ppangolin.v1.ui

import java.awt.event.ActionEvent
import java.io.FileInputStream
import java.security.MessageDigest
import javax.swing._

import net.liftweb.json._
import ppangolin.v1.misc.Str
import ppangolin.v1.sbox.HySBox
import ppangolin.v1.ui.Activities._
import ppangolin.v1.ui.misc.{HashResultPage, LoadingPage}

/**
  * by default hash string is hex encoded
  *
  * each file is hashed async (in SwingWorker), thus multiple file can be hashed at the same time
  *  this is done by dispatch works to a swing worker and create new HashResultPage in each worker
  *
  * Created by Bowen Cai on 11/6/2015.
  */
class HashFilePage(win:MainView, hySBox: HySBox) {

  private var hashSpeed: Int = 0

  def start(): Unit = {

    hashSpeed = (win.config \\ "file-hash" \ "bgtask-size-speed").asInstanceOf[JInt].values.toInt

    win.menuHash.getSubElements()(0).asInstanceOf[JPopupMenu].getSubElements // get all menu item
      .foreach(_.asInstanceOf[JMenuItem].addActionListener((e: ActionEvent) => {
      val fc = new JFileChooser()
      fc.setDialogTitle("Hash File")
      val ret = fc.showOpenDialog(win)
      if (ret == JFileChooser.FILES_ONLY) {
        val f = fc.getSelectedFile
        val fName = f.getPath
        val algo = e.getSource.asInstanceOf[JMenuItem].getText
        val flen = f.length()
        var consumed = 0
        val estTime = flen / hashSpeed // 18M/S

        val resultDialog = new HashResultPage(fName, hySBox, win, hexEncoded = true)
        resultDialog.start()
        val work = ()=>{
          val hash = try {
            val md = MessageDigest.getInstance(algo)
            processStream(new FileInputStream(f), (buf, len) => {
              md.update(buf, 0, len)
              consumed += len
            }).close()
            val bs = md.digest()
            (Str.Codec.toHexStr(bs), bs)
          } catch {
            case e: Throwable => e.printStackTrace()
              (s"Could not digest file [${f.getPath}] using $algo\r\n${e.getMessage}", Array.emptyByteArray)
          }
          resultDialog.lbLen.setText(algo + "  hash length: " + (hash._2.length * 8) + " bits")
          resultDialog.txtHash.setText(hash._1)
          resultDialog.setVisible(true)
        }
        if (estTime > 3) {
          val loadingDialog = new LoadingPage(resultDialog, "Hashing " + fName)
          loadingDialog.show(estTime.asInstanceOf[Int], () => {
            consumed.asInstanceOf[Float] / flen
          })
          new SwingWorker[Unit, Unit] {
            override def doInBackground(): Unit = work()
            override def done(): Unit = loadingDialog.dismiss()
          }.execute()
        } else work()
      }
    }))
  }
}

