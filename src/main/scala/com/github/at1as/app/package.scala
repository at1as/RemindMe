package com.github.at1as

package object lock {

  var locked: Boolean = false

  def isLocked: Boolean = {
    locked
  }

  def acquire(): Unit = {
    locked = true
  }

  def release(): Unit = {
    locked = false
  }

}
