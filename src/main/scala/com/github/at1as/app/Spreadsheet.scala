package com.github.at1as.app

import java.text.SimpleDateFormat
import java.util.Calendar

import com.github.tototoshi.csv.{CSVReader, CSVWriter}
import com.github.at1as.lock
import scala.concurrent.{Future, Promise}


object Spreadsheet {

  // Spreadsheet Format:
  //    <ID>,<DATE_ADDED>,<DATE_UPDATED>,<TO_NUMBER>,<MSG_BODY>,<SCHEDULE>,<LAST_SENT>,<ACTIVE>

  lazy val csvFile: String = new java.io.File(".").getCanonicalPath + "/data/scheduled_jobs.csv"


  def findEntryBySchedule(day: String): List[Map[String,String]] = {
    val file  = CSVReader.open(csvFile)
    val today = dateStamp()
    val matchingRows = file.allWithHeaders().filter(fields => {
      (fields("SCHEDULE").split(" ") contains day.toLowerCase) && (fields("LAST_SENT") != today)
    })

    matchingRows
  }

  def updateEntryLastSent(id: Int): Boolean = {
    // Update entry last sent date
    synchronized {
      val writer = CSVWriter.open(csvFile)

      var entryUpdated: Boolean = false

      var rows: List[List[String]] = List()
      val reader = CSVReader.open(csvFile)

      reader.foreach(fields => {
        if (fields.head == id.toString) {
          val row = List(fields.head, fields(1), dateStamp(), fields(3), fields(4), fields(5), dateStamp(), fields(7))
          entryUpdated = true
          rows +:= row
        } else {
          rows +:= fields.toList
        }
      })

      writer.writeAll(rows.reverse)

      entryUpdated
    }
  }

  def addEntry(toNumber: String, body: String, schedule: List[String]): Int = {
    // Add entry new scheduled task
    val writer = CSVWriter.open(csvFile, append = true)

    writer.synchronized {
      val rowId = rowCount() + 1
      val date = dateStamp()

      writer.writeRow(List(
        rowId,
        date,
        date,
        toNumber,
        body,
        schedule.mkString(" "),
        "",
        1
      ))

      rowId
    }
  }

  def removeEntry(from: String, id: Int): Int = {
    // Remove entry by ID. Soft delete by setting 'Active' field to false
    // Only delete given ID if number requesting it is the number the reminders are sent to
    var rowsDeleted = 0

    var rows: Array[List[Any]] = Array()
    val reader = CSVReader.open(csvFile)
    val active = 0

    reader.foreach(fields => {
      if (fields.head == id.toString && fields(3) == from) {
        val row = List(fields.head, fields(1), dateStamp(), fields(3), fields(4), fields(5), fields(6), active)
        rowsDeleted += 1
        rows +:= row
      } else {
        rows +:= fields.toList
      }
      println(rows)
    })

    val writer = CSVWriter.open(csvFile)
    writer.writeAll(rows.reverse)

    rowsDeleted
  }

  def removeAccountEntries(phonenumber: String): Int = {
    // Remove all entries matching phone-number. Soft delete by setting 'Active' field to false
    var rowsDeleted = 0

    var rows: List[List[Any]] = List()
    val reader = CSVReader.open(csvFile)
    val active = 0

    reader.foreach(fields => {
      if (fields(3) == phonenumber) {
        val row = List(fields.head, fields(1), dateStamp(), fields(3), fields(4), fields(5), fields(6), active)
        rowsDeleted += 1
        rows +:= row
      } else {
        rows +:= fields.toList
      }
    })

    val writer = CSVWriter.open(csvFile)
    writer.writeAll(rows.reverse)

    rowsDeleted

  }

  private def rowCount(): Int = {
    val file = CSVReader.open(csvFile)
    file.synchronized(file.allWithHeaders().size)
  }

  private def dateStamp(): String = {
    val dateFormat = new SimpleDateFormat("d-M-y")
    dateFormat.format(Calendar.getInstance().getTime)
  }

  private def waitForLock(attempts:Int = 3): Boolean = {
    if (!lock.isLocked) {
      lock.acquire()
      return true
    }

    var lockCheckCount: Int = 0
    while (lockCheckCount < attempts) {
      lockCheckCount += 1
      Thread.sleep(1000)
    }

    false
  }
}
