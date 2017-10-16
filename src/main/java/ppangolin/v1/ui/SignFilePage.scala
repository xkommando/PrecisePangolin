package ppangolin.v1.ui

import java.awt.event.ActionEvent
import java.io.FileInputStream
import java.security.Signature
import javax.swing._

import net.liftweb.json._
import ppangolin.v1.misc.Str.Codec._
import ppangolin.v1.sbox.HySBox
import ppangolin.v1.ui.Activities._
import ppangolin.v1.ui.misc.{HashResultPage, LoadingPage}

/**
  *
  * similar to hash file
  * each file is hashed/signed async (in SwingWorker), thus multiple file can be signed at the same time
  *
  * Created by Bowen Cai on 11/12/2015.
  */
class SignFilePage(win: MainView, hySBox: HySBox) extends JDialog(win) {

  private var hashSpeed: Int = 0

  def start(): Unit = {
    hashSpeed = (win.config \\ "file-hash" \ "bgtask-size-speed").asInstanceOf[JInt].values.toInt

    win.menuSign.getSubElements()(0).asInstanceOf[JPopupMenu].getSubElements // get all menu item
      .foreach(_.asInstanceOf[JMenuItem].addActionListener((e: ActionEvent) => {
      val fc = new JFileChooser()
      fc.setDialogTitle("Sign File")
      val ret = fc.showOpenDialog(win)
      if (ret == JFileChooser.FILES_ONLY) {
        val algo = e.getSource.asInstanceOf[JMenuItem].getText
        val f = fc.getSelectedFile
        val fName = f.getPath
        val flen = f.length()
        var consumed = 0
        val estTime = flen / hashSpeed // 18M/S

        val resultDialog = new HashResultPage(fName, hySBox, win, hexEncoded = true)
        resultDialog.start()

        val work = () => {
          val sg = try {
            val ds = Signature.getInstance(algo)
            ds.initSign(hySBox.myPri)
            processStream(new FileInputStream(f), (buf, len) => {
              ds.update(buf, 0, len)
              consumed += len
            }).close()
            val bs = ds.sign()
            (toHexStr(bs), bs)
          } catch {
            case e: Throwable => e.printStackTrace()
              (s"Could not sign file [${f.getPath}] using $algo" + exceptInfo(e), Array.emptyByteArray)
          }

          resultDialog.lbLen.setText(algo + "  signature length: " + (sg._2.length * 8) + " bits")
          resultDialog.txtHash.setText(sg._1)
          resultDialog.setVisible(true)
        }
        if (estTime > 3) {
          val loadingDialog = new LoadingPage(resultDialog, "Signing  " + fName)
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