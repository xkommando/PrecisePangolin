package ppangolin.v1.ui

import java.awt.datatransfer.{DataFlavor, StringSelection}
import java.awt.event._
import java.awt.{Color, Dimension, Toolkit}
import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import java.io.{InputStream, OutputStream}
import java.util.concurrent.Callable
import java.util.regex.Pattern
import javax.swing._
import javax.swing.border.{Border, BevelBorder}
import javax.swing.event.{UndoableEditEvent, UndoableEditListener}
import javax.swing.plaf.basic.BasicBorders.MarginBorder
import javax.swing.text.DefaultEditorKit.{CopyAction, CutAction, PasteAction}
import javax.swing.text.JTextComponent
import javax.swing.undo.UndoManager

import ppangolin.v1.Spec
import ppangolin.v1.ui.misc.CodecMenuAction

import scala.language.implicitConversions


/**
  * util functions
  *
  * Created by Bowen Cai on 11/5/2015.
  */
object Activities {

  val ENCODING = Spec.ENCODING

  val ASCII = "ASCII"

  val defaultToolkit = Toolkit.getDefaultToolkit

  /**
    * add UndoManager to the JTextComponent, enable undo redo function
    *
    * @param txt TextComponent
    * @return
    */
  def undoRedoEnabled(txt: JTextComponent): UndoManager = {
    val inputDoc = txt.getDocument
    val inputMap = txt.getInputMap
    val actMap = txt.getActionMap
    val undoMgr = new UndoManager
    inputDoc.addUndoableEditListener(new UndoableEditListener {
      override def undoableEditHappened(e: UndoableEditEvent): Unit =
        undoMgr.addEdit(e.getEdit)
    })
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit.getMenuShortcutKeyMask), "Undo")
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit.getMenuShortcutKeyMask), "Redo")
    actMap.put("Undo", new AbstractAction() {
      override def actionPerformed(e: ActionEvent): Unit = try {
        if (undoMgr.canUndo)
          undoMgr.undo()
      } catch {
        case e: Throwable => e.printStackTrace()
      }
    })
    actMap.put("Redo", new AbstractAction() {
      override def actionPerformed(e: ActionEvent): Unit = try {
        if (undoMgr.canRedo)
          undoMgr.redo()
      } catch {
        case e: Throwable => e.printStackTrace()
      }
    })
    undoMgr
  }

  /**
    * user can select and right click to edit the selected text, specificly:
    *  copy cut past
    *  encode decode base-64, hex, bin
    *
    * @param textField TextComponent
    */
  def enableEditMenu(textField: JTextComponent): Unit = {
    val copyAction = new CopyAction() //textField.getActionMap().get("copy")
    copyAction.putValue(Action.NAME, "Copy")
    val cutAction = new CutAction()
    cutAction.putValue(Action.NAME, "Cut")
    val pasteAction = new PasteAction()
    pasteAction.putValue(Action.NAME, "Paste")
    val popup = new JPopupMenu()
    popup.add(cutAction)
    popup.add(copyAction)
    popup.add(pasteAction)
    val menuDec = new JMenu("Decode")
    menuDec.add(new CodecMenuAction(2, "Base64"))
    menuDec.add(new CodecMenuAction(4, "Hex"))
    menuDec.add(new CodecMenuAction(6, "Bin"))

    val menuEnc = new JMenu("Encode")
    menuEnc.add(new CodecMenuAction(1, "Base64"))
    menuEnc.add(new CodecMenuAction(3, "Hex"))
    menuEnc.add(new CodecMenuAction(5, "Bin"))

    popup.addSeparator()
    popup.add(menuEnc)
    popup.add(menuDec)
    textField.setComponentPopupMenu(popup)
  }


  /**
    * border for JTextComponent
    */
  val borderTxt = CompoundBorder(EtchedBorder, EmptyBorder(2, 3, 2, 3)) // text margin
  /**
    * wrap component with JScrollPane
    * if it is a JTextComponent and border have not been set, set a text border (Etched Border with some margin)
    *
    * @param c to be wrapped
    * @param width JScrollPane width
    * @param higth JScrollPane hight
    * @return JScrollPane
    */
  @inline
  def scrollEnabled(c: JComponent, width:Int, higth:Int):JScrollPane = {
    val sp = new JScrollPane(c)
    sp.setPreferredSize(new Dimension(width, higth))
    if(c.isInstanceOf[JTextComponent]) {
      val brd = c.getBorder
      // if with default border (MarginBorder), set a text-border
      if (brd != null && brd.isInstanceOf[MarginBorder])
        c.setBorder(borderTxt)
    }
    sp
  }

  //---------------------------------------------------------------------------------------
  @inline
  def EmptyBorder = BorderFactory.createEmptyBorder()
  @inline
  def EmptyBorder(weight: Int) =
    BorderFactory.createEmptyBorder(weight, weight, weight, weight)
  @inline
  def EmptyBorder(top: Int, left: Int, bottom: Int, right: Int) =
    BorderFactory.createEmptyBorder(top, left, bottom, right)

  @inline
  def LineBorder(c: Color) = BorderFactory.createLineBorder(c)
  @inline
  def LineBorder(c: Color, weight: Int) = BorderFactory.createLineBorder(c, weight)
  def BeveledBorder(kind: Embossing) = BorderFactory.createBevelBorder(kind.bevelPeer)
  def BeveledBorder(kind: Embossing, highlight: Color, shadow: Color) =
    BorderFactory.createBevelBorder(kind.bevelPeer, highlight, shadow)
  def BeveledBorder(kind: Embossing,
                    highlightOuter: Color, highlightInner: Color,
                    shadowOuter: Color, shadowInner: Color) =
    BorderFactory.createBevelBorder(kind.bevelPeer,
      highlightOuter, highlightInner,
      shadowOuter, shadowInner)

  sealed abstract class Embossing {
    def bevelPeer: Int
    def etchPeer: Int
  }
  case object Lowered extends Embossing {
    def bevelPeer = BevelBorder.LOWERED
    def etchPeer = javax.swing.border.EtchedBorder.LOWERED
  }
  case object Raised extends Embossing {
    def bevelPeer = BevelBorder.RAISED
    def etchPeer = javax.swing.border.EtchedBorder.RAISED
  }

  def EtchedBorder = BorderFactory.createEtchedBorder
  def EtchedBorder(kind: Embossing) =
    BorderFactory.createEtchedBorder(kind.etchPeer)
  def EtchedBorder(kind: Embossing, highlight: Color, shadow: Color) =
    BorderFactory.createEtchedBorder(kind.etchPeer, highlight, shadow)

  def CompoundBorder(outside: Border, inside: Border) =
    BorderFactory.createCompoundBorder(outside, inside)

  def TitledBorder(border: Border, title: String) =
    BorderFactory.createTitledBorder(border, title)

  def groupBtns(btns: AbstractButton*): ButtonGroup = {
    val gp = new ButtonGroup
    for (b <- btns)
      gp.add(b)
    gp
  }
  //---------------------------------------------------------------------------------------
  @inline
  def toClipBoard(s:String): Unit = {
    val data = new StringSelection(s)
    defaultToolkit.getSystemClipboard.setContents(data, data)
  }

  @inline
  def exceptInfo(e:Throwable) = "\r\n" + e.getClass.getName + ":" + e.getMessage

  @inline
  def strFromClipBoard: String = {
    val data = defaultToolkit.getSystemClipboard.getContents(null)
    if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor))
      data.getTransferData(DataFlavor.stringFlavor).asInstanceOf[String]
    else null
  }

  /**
    * read bytes from stream to buffer(length 16384)
    * process bytes in the buffer starts from index 0 to index $len
    *
    * @param in InputStream
    * @param op operation on the stream
    */
  def processStream(in: InputStream, op:(Array[Byte], Int)=>Unit): InputStream = {
    val buf = new Array[Byte](16384)
    var len = in.read(buf, 0, 16384)
    while (len != -1) {
      op(buf, len)
      len = in.read(buf, 0, 16384)
    }
    in
  }

  def finishCopy(in: InputStream, out: OutputStream): Unit = {
    processStream(in, (buf, len)=>{
      out.write(buf, 0, len)
    })
    in.close()
    out.close()
  }

  def cleanListener(btn: {
    def getActionListeners(): Array[ActionListener]
    def removeActionListener(l: ActionListener): Unit
  }) = {
    val als = btn.getActionListeners()
    if (null != als && als.nonEmpty)
      als.foreach(btn.removeActionListener)
  }


  @inline
  implicit def makeRunnable(f: => Unit): Runnable = new Runnable() { def run() = f }

  @inline
  implicit def makeCallable[T](f: => T): Callable[T] = new Callable[T]() { def call() = f }

  @inline
  implicit def makeActionListener(f: ActionEvent => Unit): ActionListener
  = new ActionListener() { def actionPerformed(e: ActionEvent) = f(e) }

  @inline
  implicit def makeItemListener(f: ItemEvent => Unit): ItemListener
  = new ItemListener() { def itemStateChanged(e: ItemEvent) = f(e) }

  @inline
  implicit def makePropChangedListener(f:PropertyChangeEvent=>Unit): PropertyChangeListener
  = new PropertyChangeListener {
    override def propertyChange(evt: PropertyChangeEvent): Unit = f(evt)
  }

  @inline
  def onMousePress(comp: { def addMouseListener(l: MouseListener): Unit},
                   action: Either[MouseEvent, MouseEvent] => Unit): Unit = {
    comp.addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) =
        if (SwingUtilities.isRightMouseButton(e))
          action(Right(e))
        else action(Left(e))
    })
  }

  @inline
  def onToggle(btn: {def addItemListener(l: ItemListener): Unit},
               action: ItemEvent => Unit): Unit = {
    btn.addItemListener(new ItemListener {
      override def itemStateChanged(e: ItemEvent): Unit = action(e)
    })
  }

  @inline
  def onClick(btn: {def addActionListener(l: ActionListener): Unit},
              action: ActionEvent => Unit): Unit = {
    btn.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = action(e)
    })
  }

  @inline
  def onClick(btn: {def addActionListener(l: ActionListener): Unit},
              action: => Unit): Unit = {
    btn.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = action
    })
  }

}
