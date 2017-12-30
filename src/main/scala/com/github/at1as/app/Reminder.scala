package com.github.at1as.app

import java.text.SimpleDateFormat
import java.util.{Calendar, Locale}

import org.scalatra._
import com.github.at1as.app.TwilioClient.{initializeTwilio, sendMessage}

class Reminder extends ScalatraServlet with MethodOverride {

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
    // Twilio only offers a single web-hook for incoming numbers
    // which must be either GET or POST
    // so all app logic will be behind this endpoint

    val DAYS = Array(
      "monday",
      "tuesday",
      "wednesday",
      "thursday",
      "friday",
      "saturday",
      "sunday"
    )

    val from   = params("from")
    val action = params("body").split(" ").head.toLowerCase
    val text   = params("body").split(" ").tail.mkString(" ")

    if (action contains ("unsubscribe", "cancel", "stop")) {

      // If no reminder ID is passed, delete all reminders for the incoming number
      var deletedNum: Int = 0

      if (text == "") {
        deletedNum = Spreadsheet.removeAccountEntries(from)
      } else {
        deletedNum = Spreadsheet.remoteEntry(text.toInt)
      }

      initializeTwilio()
      sendMessage(from, s"Removed $deletedNum reminders")

      //} else if (action contains ("daily", "weekly", "weekdays", "weekends", DAYS : _*)) {
    } else if (action contains Array("daily", "weekly", "weekdays", "weekends", DAYS).flatMap { case s: String => Array(s); case as: Array[String] => as }) {

      var schedule: Array[String] = Array()

      action match {
        case "daily" =>
          schedule = DAYS
        case "weekly" =>
          schedule = Array(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance.getTime))
        case "weekdays" =>
          schedule = DAYS.filterNot(day => Array("saturday", "sunday") contains day)
        case "weekends" =>
          schedule = Array("saturday", "sunday")
        case _ =>
          schedule = Array(action)
      }

      Spreadsheet.addEntry(
        from,
        text,
        schedule
      )

      initializeTwilio()
      sendMessage(from, s"You are now subscribed on $action schedule to receive reminders for: $text")
    }
  }

  post("/scheduler") {
    initializeTwilio()

    // send all scheduled messages
    val dateFormat = new SimpleDateFormat("d-M-y")
    val today = dateFormat.format(Calendar.getInstance().getTime)
    val jobIds: Array[Int] = Array()

    // Find messages scheduled to send on the current day
    val messages = Spreadsheet.findEntryBySchedule(today)
    messages.foreach(fields => {
      sendMessage(
        fields("TO_NUMBER"), fields("MSG_BODY")
      )
      jobIds :+ fields("ID")
    })

    // Update "LAST_SENT" field on the job
    jobIds.foreach(id => Spreadsheet.updateEntryLastSent(id))
  }

}