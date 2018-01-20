package com.github.at1as.app

import java.text.SimpleDateFormat
import java.util.{Calendar, Locale}

import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._
import com.github.at1as.app.TwilioClient.sendMessage
import com.github.at1as.app.MessageCallback

import scala.concurrent.{ExecutionContext, Future}

class Reminder extends ScalatraServlet with MethodOverride with JacksonJsonSupport {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  get("/") {
    views.html.hello()
  }

  get("/help") {
    """
      |Usage:
      |
      | Reminders can be useful for recurring tasks ("take out the trash") or as daily reminders
      |  until the task has been completed ("file my taxes")
      |
      |
      | 1) To subscribe to reminders:
      |
      |   => Text: "<Frequency> <Message>"
      |
      |   <daily/weekly/weekdays/weekends/monday/tuesday/wednesday/thursday/friday/saturday/sunday> <message>
      |
      |   Ex.
      |     MONDAY take out the trash
      |     DAILY file my taxes
      |
      |
      | 2) To Stop receiving reminders:
      |
      |   => Text : "STOP <Reminder ID>"
      |
      |   Ex.
      |     STOP        # stop receiving all reminders to your account
      |     STOP 1234   # stop receiving reminder ID 1234 to your number
      |
    """.stripMargin
  }
  
  post("/reminder") {
    // Twilio only offers oine single web-hook for incoming numbers which must
    // be either GET or POSTso all app logic will be behind this endpoint
    new AsyncResult { val is: Unit = {
      val Days = List(
        "monday",
        "tuesday",
        "wednesday",
        "thursday",
        "friday",
        "saturday",
        "sunday"
      )

      val from   = params("From")
      val action = params("Body").split(" ").head.toLowerCase
      val text   = params("Body").split(" ").tail.mkString(" ")

      if (List("unsubscribe", "cancel", "stop", "completed").contains(action)) {

        // If no reminder ID is passed, delete all reminders for the incoming number
        var deletedNum: Int = 0

        if (text.isEmpty) {
          deletedNum = Spreadsheet.removeAccountEntries(from)
        } else {
          deletedNum = Spreadsheet.removeEntry(from, text.toInt)
        }

        sendMessage(from, s"Removed $deletedNum reminders")

      } else if ((List("daily", "weekly", "weekdays", "weekends") ::: Days).contains(action)) {

        var schedule: List[String] = List()

        action match {
          case "daily" =>
            schedule = Days
          case "weekly" =>
            schedule = List(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance.getTime))
          case "weekdays" =>
            schedule = Days.filterNot(day => List("saturday", "sunday").contains(day))
          case "weekends" =>
            schedule = List("saturday", "sunday")
          case _ =>
            schedule = List(action)
        }

        println(schedule)
        val entryId = Spreadsheet.addEntry(from, text, schedule)

        sendMessage(from, s"You are now subscribed on $action schedule to receive reminders for: $text (id: $entryId)")
      }
    }}
  }

  post("/scheduler") {
    new AsyncResult { val is: Unit = {
      // send all scheduled messages
      val dateFormat = new SimpleDateFormat("d-M-y")
      val today      = dateFormat.format(Calendar.getInstance().getTime)
      val dayOfWeek  = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance.getTime)

      var jobIds: Array[Int] = Array()

      // Find messages scheduled to send on the current day
      val messages = Spreadsheet.findEntryBySchedule(dayOfWeek)
      println(f"Batching ${messages.size} reminders to send")

      messages.foreach(fields => {
        sendMessage(
          fields("TO_NUMBER"), fields("MSG_BODY")
        )
        jobIds :+= fields("ID").toInt
      })

      // Update "LAST_SENT" field on the job
      jobIds.foreach(id => Spreadsheet.updateEntryLastSent(id))
    }}
  }

}
