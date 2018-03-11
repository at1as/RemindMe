package com.github.at1as.app

import akka.actor.Actor
import akka.actor.Actor.Receive
import com.github.at1as.app.Spreadsheet

class SpreadsheetAccessDelegator extends Actor {

  /*
    Actor mailbox will process all tasks serially.
    Therefore the spreadsheet class will not need to handle file locking
   */
  def receive: Receive = {
    case List("findEntryBySchedule", day: String) =>
      sender() ! Spreadsheet.findEntryBySchedule(day)
    case List("updateEntryLastSent", id: Int) =>
      sender() ! Spreadsheet.updateEntryLastSent(id)
    case List("addEntry", toNumber: String, fromNumber: String, schedule: List[String]) =>
      sender() ! Spreadsheet.addEntry(toNumber, fromNumber, schedule)
    case List("removeEntry", from: String, id: Int) =>
      sender() ! Spreadsheet.removeEntry(from, id)
    case List("removeAccountEntries", phonenumber: String) =>
      sender() ! Spreadsheet.removeAccountEntries(phonenumber)
  }

}
