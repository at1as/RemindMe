package com.github.at1as.app

import java.text.SimpleDateFormat
import java.util.Calendar

import com.github.tototoshi.csv.{ CSVReader, CSVWriter }

object Spreadsheet {

  // Spreadsheet Format:
  //    <ID>,<DATE_ADDED>,<DATE_UPDATED>,<TO_NUMBER>,<MSG_BODY>,<SCHEDULE>,<LAST_SENT>,<ACTIVE>

  val csvFile: String = new java.io.File(".").getCanonicalPath + "/data/scheduled_jobs.csv"

  def findEntryBySchedule(day: String): List[Map[String,String]] = {
    val file  = CSVReader.open(csvFile)
    val today = dateStamp()
    val matchingRows = file.allWithHeaders().filter(fields => {
      (fields("SCHEDULE").split(" ") contains day.toLowerCase) && (fields("LAST_SENT") != today)
    })

    matchingRows
  }

  def updateEntryLastSent(id: Int): Boolean = {
    // Remove entry by ID. Soft delete by setting 'Active' field to false
    var entryUpdated: Boolean = false

    var rows: List[List[String]]= List()
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

    val writer = CSVWriter.open(csvFile)
    writer.writeAll(rows.reverse)

    entryUpdated
  }

  def addEntry(toNumber: String, body: String, schedule: Array[String]): Int = {
    // Add entry new scheduled task
    println(f"Adding entry $toNumber // $body // $schedule")
    val rowId = rowCount() + 1
    val date  = dateStamp()
    println(f"row ID $rowId on date $date")
    val writer = CSVWriter.open(csvFile, append = true)
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

  def remoteEntry(id: Int): Int = {
    // Remove entry by ID. Soft delete by setting 'Active' field to false
    var rowsDeleted = 0

    var rows: List[List[Any]]= List()
    val reader = CSVReader.open(csvFile)
    val active = 0

    reader.foreach(fields => {
      if (fields.head == id.toString) {
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

  def removeAccountEntries(phonenumber: String): Int = {
    // Remove all entries matching phonenumber. Soft delete by setting 'Active' field to false
    var rowsDeleted = 0

    val rows: List[List[Any]]= List()
    val reader = CSVReader.open(csvFile)
    val active = 0

    reader.foreach(fields => {
      if (fields(3) == phonenumber) {
        val row = List(fields.head, fields(1), dateStamp(), fields(3), fields(4), fields(5), fields(6), active)
        rowsDeleted += 1
        rows +: row
      } else {
        rows +: fields.toList
      }
    })

    val writer = CSVWriter.open(csvFile)
    writer.writeAll(rows.reverse)

    rowsDeleted
  }

  private def rowCount(): Int = {
    val file = CSVReader.open(csvFile)
    file.allWithHeaders().size
  }

  private def dateStamp(): String = {
    val dateFormat = new SimpleDateFormat("d-M-y")
    dateFormat.format(Calendar.getInstance().getTime)
  }
}
