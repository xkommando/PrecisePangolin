package ppangolin.v1.ui.misc

import java.awt.FlowLayout
import java.awt.event.WindowEvent
import javax.swing._

import ppangolin.v1.ui.Activities._

/**
  * show loading progress, auto update progress bar
  * used in HashFilePage SignFilePage VerifySignPage
  *
  * Created by Bowen Cai on 11/10/2015.
  */
class LoadingPage(parent: JDialog, title:String) extends JDialog(parent, title, false) {

  private val lbEstTime = new JLabel()
  private val progBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100)
  private val btnGotoBg = new JButton("Do in background")
  // timer to update progress bar
  private val tmProgbar = new Timer(100, null)//60

  // time interval to update $time remaining
  private val timeInterval = 800 // ms
  // timer  to update $time remaining
  private val tmRemaining = new Timer(timeInterval, null)//60

  setLocation(500, 250)
  setSize(360, 160)
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

  //-------------------------------------------------------
  {
    val _p2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5))
    _p2.setBorder(EmptyBorder(10, 30, 15, 30))
    _p2.add(lbEstTime)
    _p2.add(progBar)
    _p2.add(btnGotoBg)
    add(_p2)
  }

  progBar.setStringPainted(true)
  tmProgbar.stop()
  tmRemaining.stop()
  onClick(btnGotoBg, _ => dismiss())

  /**
    *
    * @param timeEst estimated time in total
    * @param f update function, when called it should return a float ranging from 0 to 1 indicating current progress
    */
  def show(timeEst: Int, f: () => Float): Unit = {
    lbEstTime.setText("Estimated Time: " + timeEst + ".00 seconds ")
    cleanListener(tmProgbar)
    onClick(tmProgbar, {
      val p = f() * 100
      if (p > 99.99f) {
        tmProgbar.stop()
        tmRemaining.stop()
        setVisible(false)
      } else {
        progBar.setValue(p.asInstanceOf[Int])
      }
    })
    tmProgbar.start()

    cleanListener(tmRemaining)
    var timePassed = 0.0f // sec
    var last = 0.0f // range 0~1
    val intervalSec = timeInterval.asInstanceOf[Float] / 1000
    onClick(tmRemaining, {
      timePassed += intervalSec
      val cur = f()
      val prog = cur - last
      last = cur
      val rm = 1 / prog * timeInterval / 1000 - timePassed
      lbEstTime.setText(f"Estimated Time: $rm%1.2f second")
//      println(s"passed $timePassed   last $last  cur $cur")
    })
    tmRemaining.start()
    setVisible(true)
  }

  /**
    *  just show estimated time in total, no progress bar
    * @param timeEst estimated time in total
    */
  def show(timeEst:Int): Unit = {
    lbEstTime.setText("Estimated Time: " + timeEst + "s")
    setVisible(true)
  }

  def dismiss(): Unit = {
    tmProgbar.stop()
    tmRemaining.stop()
    setVisible(false)
    dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING))
    dispose()
  }
}
