package ppangolin.v1

import java.security.{Provider, Security}

import net.liftweb.json.JsonAST.{JString, JValue}
import ppangolin.v1.sbox._
import ppangolin.v1.ui._
import ppangolin.v1.ui.msglog.MsgLoggerActivity


/*
pub enter keys            ???
loading page              OK
more algo combinations    ???
logs                      on going

mac cert

operations:
encrypt decrypt str
encrypt decrypt file
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
 * Created by Bowen Cai on 11/3/2015.
 */
object PP {

  def main(args: Array[String]): Unit = {

//-----------------------------------------------------------------------------
//          First. load configurations

    val gcfg = Spec.cfg

    val cfgbox = gcfg \ "sbox"
    // add providers
    Spec.strArr(cfgbox \ "providers").foreach(k=>{
      Security.addProvider(Class.forName(k).newInstance().asInstanceOf[Provider])
    })

    def str(jv:JValue) = jv.asInstanceOf[JString].values

    /**
      * create the box
      */
    val hySBox = new HySBox(new AsymBox(str(cfgbox \ "asymmetric" \ "keySpec"), str(cfgbox \ "asymmetric" \ "cipherName")),
                            new SymBox(str(cfgbox \ "symmetric" \ "keySpec"), str(cfgbox \ "symmetric" \ "cipherName")))

//    println(hySBox.asybox.keySpec)
//    println(hySBox.asybox.cipherName)
//    println(hySBox.symbox.keySpec)
//    println(hySBox.symbox.cipherName)

//-----------------------------------------------------------------------------
//          Sec. boot up
    /**
      * 1. create all views / pages
      */
    val ui = new MainView(hySBox, str(gcfg \ "app" \ "name") + " - v" + str(gcfg \ "app" \ "version"))

    val secKeyPage = new SecKeyPage(ui, hySBox)
    val kpa = new KeyPairPage(ui, hySBox)
    val fileSign = new SignFilePage(ui, hySBox)
    val verifySign = new VerifySignPage(ui, hySBox)
    val hashPage = new HashFilePage(ui, hySBox)

    /**
      * 2. link views/pages together
      */
    ui.config = gcfg \ "ui"
    ui.keyPairPage = kpa
    ui.secKeyPage = secKeyPage
    ui.signFilePage = fileSign
    ui.verifySignPage = verifySign

    /**
      * 3. init ui
      * main ui must start first
      */
    ui.start()

    /**
      * 4. create (pure) activities (no view elements)
      */
    val mainActivity = new MainActivity(ui, hySBox)
    val msgLogActivity = new MsgLoggerActivity(ui, mainActivity)
    mainActivity.msgLogger = msgLogActivity.msgLogger

    /**
      * 5. start all activities
      */
    mainActivity.start()
    msgLogActivity.start()
    secKeyPage.start()
    kpa.start()
    fileSign.start()
    verifySign.start()
    hashPage.start()
  }
}
