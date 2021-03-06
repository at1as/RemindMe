package com.github.at1as.app

import com.twilio.Twilio
import com.twilio.`type`.PhoneNumber
import com.typesafe.config.{Config, ConfigFactory}
import com.twilio.rest.api.v2010.account.Message

import java.io.File
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

object TwilioClient {

  val initializeTwilio: Unit = {
    val conf = getConf

    val ACCOUNT_SID = conf.getString("twilio_account.account_sid")
    val AUTH_TOKEN  = conf.getString("twilio_account.auth_token")

    Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
  }

  def sendMessage(toNumber: String, body: String)(implicit ctx: ExecutionContext): Future[Unit] = {
    Future {
      val conf = getConf

      val from = new PhoneNumber(conf.getString("twilio_account.from_number"))
      val to   = new PhoneNumber(toNumber)

      Try(Message.creator(to, from, body).create()) match {
        case Success(m) =>
          val msg = s"Message sent to $to with message SID ${m.getSid}"
          println(msg)

        case Failure(e) =>
          println(s"Encountered exception: \n${e.getMessage}")
      }
    }
  }

  private lazy val getConf: Config = {
    val filename = new java.io.File(".").getCanonicalPath + "/src/main/resources/twilio_account.conf"
    val parsedConfig = ConfigFactory.parseFile(new File(filename))

    ConfigFactory.load(parsedConfig)
  }

}
