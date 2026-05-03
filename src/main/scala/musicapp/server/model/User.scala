package musicapp.server.model

case class User(
  email: String,
  passwordHash: String
)

object User {
  def fromString(line: String): Option[User] = {
    val parts = line.split(",")
    if (parts.length == 2) Some(User(parts(0), parts(1)))
    else None
  }

  def toString(user: User): String = {
    s"${user.email},${user.passwordHash}"
  }
}
