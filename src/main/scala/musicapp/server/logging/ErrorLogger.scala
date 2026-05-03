package musicapp.server.logging

import java.io.{File, FileWriter, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ErrorLogger(logFilePath: String) {
  private val file = new File(logFilePath)
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def logError(e: Throwable, user: String = "system", context: String = ""): Unit = {
    val writer = new PrintWriter(new FileWriter(file, true))
    try {
      val timestamp = LocalDateTime.now().format(formatter)
      writer.println(s"[$timestamp] ERROR for user: $user")
      if (context.nonEmpty) writer.println(s"Context: $context")
      writer.println(s"Exception: ${e.getClass.getName}: ${e.getMessage}")
      e.getStackTrace.foreach(trace => writer.println(s"  at $trace"))
      writer.println("-" * 80)
    } finally {
      writer.close()
    }
  }

  def logMessage(message: String): Unit = {
    val writer = new PrintWriter(new FileWriter(file, true))
    try {
      val timestamp = LocalDateTime.now().format(formatter)
      writer.println(s"[$timestamp] $message")
    } finally {
      writer.close()
    }
  }
}
