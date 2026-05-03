package musicapp.server.storage

import musicapp.server.model.User
import java.io.{File, PrintWriter}
import scala.io.Source

class UserStorage(filePath: String) {
  private val file = new File(filePath)
  private var users: Map[String, User] = loadUsers()

  private def loadUsers(): Map[String, User] = {
    if (!file.exists()) {
      Map.empty
    } else {
      val source = Source.fromFile(file)
      try {
        source.getLines()
          .flatMap(User.fromString)
          .map(u => u.email -> u)
          .toMap
      } finally {
        source.close()
      }
    }
  }

  private def saveUsers(): Unit = {
    val writer = new PrintWriter(file)
    try {
      users.values.foreach(u => writer.println(User.toString(u)))
    } finally {
      writer.close()
    }
  }

  def register(email: String, password: String): Boolean = {
    if (users.contains(email)) {
      false
    } else {
      val hashedPassword = hashPassword(password)
      val user = User(email, hashedPassword)
      users = users + (email -> user)
      saveUsers()
      true
    }
  }

  def login(email: String, password: String): Boolean = {
    users.get(email) match {
      case Some(user) => user.passwordHash == hashPassword(password)
      case None => false
    }
  }

  def exists(email: String): Boolean = users.contains(email)

  private def hashPassword(password: String): String = {
    // Simple hash for demonstration - in production use proper hashing
    java.util.Base64.getEncoder.encodeToString(
      java.security.MessageDigest.getInstance("SHA-256")
        .digest(password.getBytes("UTF-8"))
    )
  }
}
