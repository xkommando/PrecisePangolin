package ppangolin.v1.ui.msglog

import java.awt.{Color, Component}
import javax.swing._

import scala.collection.TraversableOnce
import scala.collection.mutable.ListBuffer

/*

task:
timestamp

encry decry str
encry decry file
hash str
hash file
sign str
sign file
v sign str
v sign file

load pub keys / full keys
save pub keys / full keys

gen key pair
enter pub key

remove key pair
remove pub key

gen sec key
enter sec key
remove sec key

 */

/**
  *
  * MsgLogger is also a ListModel
  * Created by Bowen Cai on 11/23/2015.
  */
class MsgLogger extends AbstractListModel[MsgLog] {

  private var ls = new ListBuffer[MsgLog]()

  override def getElementAt(index: Int): MsgLog = ls(index)
  override def getSize: Int = ls.size

  def prepend(element: MsgLog): this.type = {
    val index = ls.size
    ls prepend  element
    fireIntervalAdded(this, index, index)
    this
  }

  def insert(lg: MsgLog): this.type = {
    val oldSz = ls.size
    val idx = ls.indexWhere(_.time < lg.time)
    if (idx >= 0)
      ls.insert(idx, lg)
    else
      ls.append(lg)
    fireIntervalAdded(this, oldSz, oldSz)
    this
  }

  def ++= (xs: TraversableOnce[MsgLog]) = addAll(xs)

  def addAll(xs: TraversableOnce[MsgLog]): this.type = {
    ls.appendAll(xs)
    ls = ls.sortWith(_.time > _.time)
    fireContentsChanged(this, 0, ls.length)
    this
  }

  def getLogArray: Array[MsgLog] = ls.toArray

  def remove(index: Int): Unit = {
    ls.remove(index)
    fireIntervalRemoved(this, index, index)
  }

  def clear(): Unit = {
    val index1 = ls.size - 1
    ls.clear()
    if (index1 >= 0)
      fireIntervalRemoved(this, 0, index1)
  }

  def nonEmpty: Boolean = !isEmpty
  def isEmpty: Boolean = ls.isEmpty
}

/**
  * encryption -> light grey background
  * decryption -> white
  * selected -> blue
  */
class LogCellRenderer extends JLabel with ListCellRenderer[MsgLog] {

  setOpaque(true)
  private val colorSelected = new Color(210, 233, 255)

  override def getListCellRendererComponent(list: JList[_ <: MsgLog],
                                            log: MsgLog,
                                            index: Int,
                                            isSelected: Boolean,
                                            cellHasFocus: Boolean): Component = {
    /**
      * why not add tool tip to each label (message log)?
      * tool tip on cell could not always be displayed in the right position (too low)
      */
//    this.setToolTipText(log.toolTip)

    setText(log.digest)
    if (isSelected) setBackground(colorSelected)
    else if (log.asInstanceOf[TextMsgLog].isEncrypt) setBackground(Color.LIGHT_GRAY)
    else setBackground(Color.WHITE)
    this
  }
}