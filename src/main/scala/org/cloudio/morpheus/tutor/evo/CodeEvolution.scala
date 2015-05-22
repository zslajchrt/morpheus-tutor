//package org.cloudio.morpheus.tutor.evo
//
//import org.morpheus.annot.{dimension, wrapper, fragment}
//import Morpheus._
//import org.morpheus._
//
///**
// * Created by zslajchrt on 04/05/15.
// */
//class CodeEvolution {
//
//}
//
//
///**
// * Created by zslajchrt on 30/04/15.
// */
//
//class PrintCtx(fragRef: ~&[\?[Documentation]]) {
//  val fragMorph = *(fragRef).~
//  fragMorph.startListening()
//
//  def hasDocs: Boolean = {
//    select[Documentation](fragMorph).isDefined
//  }
//  //def hasDocs: Boolean = false
//
//  def p(codeElement: CodeElement): String = {
//    codeElement.code
//  }
//
//  def d(codeElement: CodeElement): String = {
//    if (codeElement.documentation.isDefined && hasDocs) {
//      s"""
//      /*
//        ${codeElement.documentation.get}
//      */
//      """
//    } else {
//      ""
//    }
//  }
//
//}
//
//case class CodeElement(code: String, documentation: Option[String] = None) {
//}
//
//import CodeElement._
//
//trait FragmentExt {
//  val name: CodeElement
//  def imports: Vector[CodeElement] = Vector.empty
//  def parents: Vector[CodeElement] = Vector.empty
//  def members: Vector[CodeElement] = Vector.empty
//  def header: Vector[CodeElement] = Vector.empty
//  def footer: Vector[CodeElement] = Vector.empty
//}
//
//@fragment
//trait Documentation
//
//@fragment
//trait Fragment {
//  this: FragmentExt with \?[Documentation] =>
//
//  lazy val self = &&(this)
//
//  lazy val ctx = new PrintCtx(*(self))
//
//  def code() = {
//    import ctx._
//    s"""
//    ${header map p mkString "\n"}
//
//    import Morpheus._
//    ${imports map p mkString ","}
//
//    ${d(name)}
//    @fragment
//    trait ${p(name)} ${if (parents.isEmpty) "" else {"extends " + parents.map(p).mkString(" with ")}} {
//    \t${members map(m => { d(m) + "\n\t\t" + p(m)}) mkString "\n\t\t"}
//    }
//
//    ${footer map p mkString "\n"}
//   """
//  }
//
//}
//
//@fragment
//trait ContactBase extends FragmentExt {
//  val name = CodeElement("Contact")
//}
//
//@fragment @wrapper
//trait Contact1 extends ContactBase {
//
//  override def members = super.members :+ CodeElement("var x: Int = 0", Some("x docs"))
//
//}
//
//@fragment @wrapper
//trait Contact2 extends Contact1 {
//
//  this: EmailService =>
//
//  override def members = super.members :+ CodeElement("var y: Int = 1", Some("y docs"))
//
//  override def parents = super.members :+ CodeElement("AnyRef")
//
//  override def footer = super.footer :+ CodeElement(emailCode())
//}
//
//@dimension
//trait EmailService {
//  def emailCode(): String
//}
//
//@fragment
//trait EmailServiceBase extends FragmentExt with EmailService {
//  this: Fragment =>
//
//  val name = CodeElement("EmailService")
//
//  override def members = Vector(CodeElement(sendMail))
//
//  def sendMail = """
//     def sendMail(to: String, msg: String) {
//     // todo
//     }
//                 """
//
//  def emailCode() = code()
//}
//
//@fragment @wrapper
//trait EmailService1 extends EmailServiceBase {
//  this: Fragment =>
//
//  override def imports = super.imports :+ CodeElement("import javax.mail._")
//
//  override def sendMail = """
//     def sendMail(to: String, msg: String) {
//       JavaMail.send(to, msg)
//     }
//                          """
//
//}
//
//object Lesson1 {
//
//  def main(args: Array[String]) {
//
//    // Models
//    type EmailEvo = EmailServiceBase with \?[EmailService1]
//    val emailModel = parse[Fragment with EmailEvo with /?[Documentation] with MutableFragment](true)
//
//    type ContactEvo = ContactBase with \?[Contact1 with \?[Contact2 with EmailService]]
//    val contactModel = parse[Fragment with ContactEvo with /?[Documentation] with MutableFragment](true)
//
//    // Evolution sequence
//
//    case class Step(contact: Int = 0, email: Int = 0)
//    var step = Step()
//    val evolution = List(
//      Step(0, 0),
//      Step(1, 0),
//      Step(2, 0),
//      Step(2, 1)
//    )
//    var doc = true
//
//    // Instances
//    val signalMonitor = EventMonitor[Int]("signal")
//    implicit val mutFrgCfg = mutableFragment()
//
//    val emailInst = singleton(emailModel, rate[EmailEvo](RootStrategy(emailModel), () => Some(step.email)))
//    val emailMorph = emailInst.~
//    emailMorph.startListening()
//    implicit val emailService = external[EmailService](emailMorph)
//
//    val tutStr1 = rate[ContactEvo](RootStrategy(contactModel), () => Some(step.contact))
//    val tutStr2 = rate[/?[Documentation]](tutStr1, () => Some(if (doc) 0 else 1))
//    val tutorialInst = singleton(contactModel, tutStr2)
//    val tutMorph = tutorialInst.~
//    tutMorph.startListening()
//
//    // Evolution
//
//    def evol(): Unit = {
//      for (next <- evolution) {
//        step = next
//        tutMorph.fire("signal", null, null)
//        println("*************************")
//        println(tutMorph.code())
//      }
//    }
//
//    evol()
//
//    doc = false
//    tutMorph.fire("signal", null, null)
//
//    evol()
//  }
//}
