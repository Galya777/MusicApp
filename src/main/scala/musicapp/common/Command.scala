package musicapp.common

sealed trait Command

object Command {
  case class Register(email: String, password: String) extends Command
  case class Login(email: String, password: String) extends Command
  case object Disconnect extends Command
  case class Search(words: String) extends Command
  case class Top(number: Int) extends Command
  case class CreatePlaylist(name: String) extends Command
  case class AddSongTo(playlistName: String, song: String) extends Command
  case class ShowPlaylist(name: String) extends Command
  case class Play(song: String) extends Command
  case object Stop extends Command

  def parse(input: String): Either[String, Command] = {
    val parts = input.trim.split("\\s+")
    if (parts.isEmpty) return Left("Empty command")

    parts(0).toLowerCase match {
      case "register" =>
        if (parts.length != 3) Left("Usage: register <email> <password>")
        else Right(Register(parts(1), parts(2)))

      case "login" =>
        if (parts.length != 3) Left("Usage: login <email> <password>")
        else Right(Login(parts(1), parts(2)))

      case "disconnect" =>
        Right(Disconnect)

      case "search" =>
        if (parts.length < 2) Left("Usage: search <words>")
        else Right(Search(parts.drop(1).mkString(" ")))

      case "top" =>
        if (parts.length != 2) Left("Usage: top <number>")
        else {
          try {
            val n = parts(1).toInt
            if (n <= 0) Left("Number must be positive")
            else Right(Top(n))
          } catch {
            case _: NumberFormatException => Left("Invalid number")
          }
        }

      case "create-playlist" =>
        if (parts.length < 2) Left("Usage: create-playlist <name_of_the_playlist>")
        else Right(CreatePlaylist(parts.drop(1).mkString(" ")))

      case "add-song-to" =>
        if (parts.length < 3) Left("Usage: add-song-to <name_of_the_playlist> <song>")
        else Right(AddSongTo(parts(1), parts.drop(2).mkString(" ")))

      case "show-playlist" =>
        if (parts.length < 2) Left("Usage: show-playlist <name_of_the_playlist>")
        else Right(ShowPlaylist(parts.drop(1).mkString(" ")))

      case "play" =>
        if (parts.length < 2) Left("Usage: play <song>")
        else Right(Play(parts.drop(1).mkString(" ")))

      case "stop" =>
        Right(Stop)

      case cmd =>
        Left(s"Unknown command: $cmd")
    }
  }
}
