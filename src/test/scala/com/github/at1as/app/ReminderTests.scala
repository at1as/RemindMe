package com.github.at1as.app

import org.scalatra.test.scalatest._
import org.scalatest.FunSuiteLike

class ReminderTests extends ScalatraSuite with FunSuiteLike {

  addServlet(classOf[Reminder], "/*")

  test("GET / on Reminder should return status 200"){
    get("/"){
      status should equal (200)
    }
  }

}
