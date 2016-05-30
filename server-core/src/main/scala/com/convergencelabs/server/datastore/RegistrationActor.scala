package com.convergencelabs.server.datastore

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.ActorLogging
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.ActorRef
import com.convergencelabs.server.datastore.RegistrationActor.RegisterUser
import com.convergencelabs.server.datastore.RegistrationActor.AddRegistration
import com.convergencelabs.server.datastore.RegistrationActor.ApproveRegistration
import java.util.UUID
import com.convergencelabs.server.datastore.ConvergenceUserManagerActor.CreateConvergenceUserRequest
import akka.actor.ActorRef
import scala.util.Try
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool
import scala.util.Success
import scala.util.Failure
import org.apache.commons.mail.HtmlEmail
import scala.concurrent.duration.FiniteDuration
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail
import com.typesafe.config.Config
import com.convergencelabs.server.datastore.RegistrationActor.RejectRegistration
import com.convergencelabs.templates.email.html
import java.net.URLEncoder

object RegistrationActor {
  def props(dbPool: OPartitionedDatabasePool, userManager: ActorRef): Props = Props(new RegistrationActor(dbPool, userManager))

  case class RegisterUser(username: String, fname: String, lname: String, email: String, password: String, token: String)
  case class AddRegistration(fname: String, lname: String, email: String)
  case class ApproveRegistration(token: String)
  case class RejectRegistration(token: String)
}

class RegistrationActor private[datastore] (dbPool: OPartitionedDatabasePool, userManager: ActorRef) extends StoreActor with ActorLogging {

  // FIXME: Read this from configuration
  private[this] implicit val requstTimeout = Timeout(15 seconds)
  private[this] implicit val exectionContext = context.dispatcher

  private[this] val smtpConfig: Config = context.system.settings.config.getConfig("convergence.smtp")
  private[this] val username = smtpConfig.getString("username")
  private[this] val password = smtpConfig.getString("password")
  private[this] val toAddress = smtpConfig.getString("toAddress")
  private[this] val fromAddress = smtpConfig.getString("fromAddress")
  private[this] val host = smtpConfig.getString("host")
  private[this] val port = smtpConfig.getInt("port")
  
  private[this] val restServerConfig = context.system.settings.config.getConfig("convergence.rest")
  private[this] val restServerUrl = s"http://${restServerConfig.getString("host")}:${restServerConfig.getInt("port")}";
  
  private[this] val adminUiServerConfig = context.system.settings.config.getConfig("convergence.admin-ui")
  private[this] val adminUiServerUrl = s"http://${adminUiServerConfig.getString("host")}:${adminUiServerConfig.getInt("port")}";

  private[this] val registrationStore = new RegistrationStore(dbPool)

  def receive: Receive = {
    case message: RegisterUser        => registerUser(message)
    case message: AddRegistration     => addRegistration(message)
    case message: ApproveRegistration => appoveRegistration(message)
    case message: RejectRegistration  => rejectRegistration(message)
    case message: Any                 => unhandled(message)
  }

  def registerUser(message: RegisterUser): Unit = {
    val origSender = sender
    val RegisterUser(username, fname, lname, email, password, token) = message
    registrationStore.isRegistrationApproved(email, token).map {
      case Some(true) => {
        val req = CreateConvergenceUserRequest(username, password, email, fname, lname)
        (userManager ? req).mapTo[CreateResult[String]] onSuccess {
          case result: CreateSuccess[String] => {
            registrationStore.removeRegistration(token)
            origSender ! result
          }
          case _ => origSender ! _
        }
      }
      case _ => origSender ! InvalidValue
    }
  }

  def addRegistration(message: AddRegistration): Unit = {
    val AddRegistration(fname, lname, email) = message
    reply(registrationStore.addRegistration(fname, lname, email) map {
      case CreateSuccess(token) => {
        val templateHtml = html.registrationRequest(token, fname, lname, email, restServerUrl)

        val approvalEmail = new HtmlEmail()
        approvalEmail.setHostName(host)
        approvalEmail.setSmtpPort(port)
        approvalEmail.setAuthenticator(new DefaultAuthenticator(username, password))
        approvalEmail.setFrom(fromAddress)
        approvalEmail.setSubject(s"Registration Request from ${fname} ${lname}")
        approvalEmail.setHtmlMsg(templateHtml.toString())
        approvalEmail.setTextMsg(s"Approval Link: ${restServerUrl}/approval/${token}")
        approvalEmail.addTo(toAddress)
        approvalEmail.send()

        CreateSuccess(Unit)
      }
      case DuplicateValue => DuplicateValue
      case InvalidValue   => InvalidValue
    })

  }

  def appoveRegistration(message: ApproveRegistration): Unit = {
    val ApproveRegistration(token) = message

    val resp = registrationStore.approveRegistration(token)
    reply(resp)
    resp match {
      case Success(UpdateSuccess) => {
        registrationStore.getRegistrationInfo(token) match {
          case Success(Some(registration)) => {
            val (firstName, lastName, email) = registration
            
            val fnameEncoded = URLEncoder.encode(firstName, "UTF8")
            val lnameEncoded = URLEncoder.encode(lastName, "UTF8")
            val emailEncoded = URLEncoder.encode(email, "UTF8")
            val signupUrl = s"${adminUiServerUrl}/signup/${token}?fname=${fnameEncoded}&lname=${lnameEncoded}&email=${emailEncoded}"
            val welcomeTxt = if(firstName != null && firstName.nonEmpty) s"${firstName}, welcome" else "Welcome"
            
            val templateHtml = html.registrationApproved(signupUrl, welcomeTxt)

            val approvalEmail = new HtmlEmail()
            approvalEmail.setHostName(host)
            approvalEmail.setSmtpPort(port)
            approvalEmail.setAuthenticator(new DefaultAuthenticator(username, password))
            approvalEmail.setFrom(fromAddress)
            approvalEmail.setSubject(s"${welcomeTxt} to Convergence!")
            approvalEmail.setHtmlMsg(templateHtml.toString())
            approvalEmail.setTextMsg(s"Signup Link: ${signupUrl}")
            approvalEmail.addTo(email)
            approvalEmail.send()
          }
          case _ => log.error("Unable to lookup registration to send Email")
        }
      }
      case _ => // Do Nothing, we have already replied with this error
    }
  }

  def rejectRegistration(message: RejectRegistration): Unit = {
    val RejectRegistration(token) = message
    reply(registrationStore.removeRegistration(token))
  }
}
