package musicapp.client

import musicapp.common.{Command, Response}
import musicapp.client.network.ServerConnection

import java.io.{File, FileWriter, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.io.StdIn

class CliInterface(connection: ServerConnection) {
  private var running = true
  private var loggedIn = false
  private val logFile = new File("client_errors.log")
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def start(): Unit = {
    println("Music App Client")
    println("================")
    println("Type 'help' for available commands")
    println()

    while (running) {
      print(if (loggedIn) "> " else "login> ")
      val input = StdIn.readLine()

      if (input != null) {
        handleInput(input.trim)
      } else {
        running = false
      }
    }
  }

  private def handleInput(input: String): Unit = {
    if (input.isEmpty) return

    val parts = input.split("\\s+")
    val cmd = parts(0).toLowerCase

    cmd match {
      case "help" =>
        printHelp()

      case "exit" | "quit" =>
        if (connection != null) {
          connection.disconnect()
        }
        running = false
        println("Goodbye!")

      case _ =>
        Command.parse(input) match {
          case Right(command) =>
            handleCommand(command)
          case Left(error) =>
            println(s"Error: $error")
        }
    }
  }

  private def handleCommand(command: Command): Unit = {
    try {
      command match {
        case Command.Register(email, password) =>
          val response = connection.sendCommand(command)
          handleResponse(response)

        case Command.Login(email, password) =>
          val response = connection.sendCommand(command)
          response match {
            case Response.Success(msg) =>
              loggedIn = true
              println(msg)
            case _ =>
              handleResponse(response)
          }

        case Command.Disconnect =>
          val response = connection.sendCommand(command)
          loggedIn = false
          handleResponse(response)

        case Command.Search(_) =>
          val response = connection.sendCommand(command)
          handleResponse(response)

        case Command.Top(_) =>
          val response = connection.sendCommand(command)
          handleResponse(response)

        case Command.CreatePlaylist(_) =>
          if (!loggedIn) {
            println("Error: You must be logged in to create a playlist")
          } else {
            val response = connection.sendCommand(command)
            handleResponse(response)
          }

        case Command.AddSongTo(_, _) =>
          if (!loggedIn) {
            println("Error: You must be logged in to add songs")
          } else {
            val response = connection.sendCommand(command)
            handleResponse(response)
          }

        case Command.ShowPlaylist(_) =>
          if (!loggedIn) {
            println("Error: You must be logged in to view playlists")
          } else {
            val response = connection.sendCommand(command)
            handleResponse(response)
          }

        case Command.Play(_) =>
          if (!loggedIn) {
            println("Error: You must be logged in to play songs")
          } else {
            val response = connection.sendCommand(command)
            handleResponse(response)
          }

        case Command.Stop =>
          connection.stopStreaming()
          val response = connection.sendCommand(command)
          handleResponse(response)
      }
    } catch {
      case e: Exception =>
        logError(e, command.toString)
        println("An error occurred while processing your command.")
        println("Details have been logged to client_errors.log")
    }
  }

  private def handleResponse(response: Response): Unit = {
    response match {
      case Response.Success(msg) =>
        println(msg)
      case Response.Error(msg) =>
        println(s"Error: $msg")
      case Response.SearchResults(songs) =>
        if (songs.isEmpty) {
          println("No songs found")
        } else {
          println(s"Found ${songs.size} song(s):")
          songs.zipWithIndex.foreach { case (song, i) =>
            println(s"  ${i + 1}. $song")
          }
        }
      case Response.TopSongs(songs) =>
        if (songs.isEmpty) {
          println("No play statistics available yet")
        } else {
          println("Top songs:")
          songs.zipWithIndex.foreach { case ((song, count), i) =>
            println(s"  ${i + 1}. $song ($count plays)")
          }
        }
      case Response.PlaylistInfo(name, songs) =>
        println(s"Playlist: $name")
        if (songs.isEmpty) {
          println("  (empty)")
        } else {
          songs.zipWithIndex.foreach { case (song, i) =>
            println(s"  ${i + 1}. $song")
          }
        }
      case Response.StreamingReady(_) =>
        println("Started playing...")
      case Response.StreamingStopped =>
        println("Playback stopped")
      case Response.Ack =>
        // No output for ACK
    }
  }

  private def printHelp(): Unit = {
    println("Available commands:")
    println("  register <email> <password>  - Register a new account")
    println("  login <email> <password>     - Login to your account")
    println("  disconnect                   - Disconnect from account")
    println("  search <words>               - Search for songs")
    println("  top <number>                 - Show top N most played songs")
    println("  create-playlist <name>       - Create a new playlist")
    println("  add-song-to <playlist> <song> - Add a song to a playlist")
    println("  show-playlist <name>         - Show playlist contents")
    println("  play <song>                  - Play a song")
    println("  stop                         - Stop playback")
    println("  exit | quit                  - Exit the application")
    println()
    println("Note: Commands marked with * require login")
  }

  private def logError(e: Throwable, context: String): Unit = {
    val writer = new PrintWriter(new FileWriter(logFile, true))
    try {
      val timestamp = LocalDateTime.now().format(formatter)
      writer.println(s"[$timestamp] Error during command: $context")
      writer.println(s"Exception: ${e.getClass.getName}: ${e.getMessage}")
      e.getStackTrace.foreach(trace => writer.println(s"  at $trace"))
      writer.println("-" * 80)
    } finally {
      writer.close()
    }
  }
}
