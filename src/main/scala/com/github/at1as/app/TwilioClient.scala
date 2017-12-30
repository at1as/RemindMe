package com.github.at1as.app

import com.twilio.Twilio
import com.twilio.`type`.PhoneNumber
import com.typesafe.config.ConfigFactory
import com.twilio.rest.api.v2010.account.Message

import scala.util.{Failure, Success, Try}

object TwilioClient {

  def initializeTwilio(): Unit = {
    val conf = ConfigFactory.load()

    val ACCOUNT_SID = conf.getString("twilio.account_sid")
    val AUTH_TOKEN  = conf.getString("twilio.auth_token")

    Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
  }

  def sendMessage(toNumber: String, body: String): Unit = {
    val conf = ConfigFactory.load()

    val from = new PhoneNumber(conf.getString("twilio.from_number"))
    val to   = new PhoneNumber(toNumber)

    Try(Message.creator(to, from, body).create()) match {
      case Success(m) => println(s"Message sent to $to with message SID ${m.getSid}")
      case Failure(e) => println(s"Encountered exception: \n${e.getMessage}")
    }
  }

}
