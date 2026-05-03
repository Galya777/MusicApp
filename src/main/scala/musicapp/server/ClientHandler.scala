package musicapp.server

import musicapp.common.{Command, Response}
import musicapp.server.audio.AudioStreamer
import musicapp.server.storage.{PlaylistStorage, SongStorage, UserStorage}
import musicapp.server.logging.ErrorLogger

import java.io.{DataInputStream, DataOutputStream, IOException}
import java.net.ServerSocket
import java.net.Socket

class ClientHandler(
  socket: Socket,
  userStorage: UserStorage,
  songStorage: SongStorage,
  playlistStorage: PlaylistStorage,
  errorLogger: ErrorLogger
) extends Runnable {

  private var currentUser: Option[String] = None
  private var running = true
  private var activeStream: Option[(AudioStreamer, ServerSocket, Thread)] = None

  override def run(): Unit = {
    try {
      val input = new DataInputStream(socket.getInputStream)
      val output = new DataOutputStream(socket.getOutputStream)

      while (running && !socket.isClosed) {
        try {
          val commandStr = input.readUTF()
          Command.parse(commandStr) match {
            case Right(command) =>
              val response = handleCommand(command)
              Response.writeToStream(response, output)

              command match {
                case Command.Disconnect =>
                  running = false
                case _ =>
              }
            case Left(error) =>
              Response.writeToStream(Response.Error(error), output)
          }
        } catch {
          case _: IOException =>
            running = false
          case e: Exception =>
            errorLogger.logError(e, currentUser.getOrElse("anonymous"))
            Response.writeToStream(
              Response.Error("An unexpected error occurred. Please try again."),
              output
            )
        }
      }

      stopActiveStream()
      socket.close()
    } catch {
      case e: Exception =>
        errorLogger.logError(e, currentUser.getOrElse("anonymous"))
    }
  }

  private def handleCommand(command: Command): Response = {
    command match {
      case Command.Register(email, password) =>
        if (userStorage.register(email, password)) {
          Response.Success(s"User $email registered successfully")
        } else {
          Response.Error(s"User $email already exists")
        }

      case Command.Login(email, password) =>
        if (userStorage.login(email, password)) {
          currentUser = Some(email)
          Response.Success(s"Welcome back, $email!")
        } else {
          Response.Error("Invalid email or password")
        }

      case Command.Disconnect =>
        currentUser = None
        Response.Success("Disconnected successfully")

      case Command.Search(words) =>
        val songs = songStorage.search(words).map(_.displayName)
        Response.SearchResults(songs)

      case Command.Top(number) =>
        val top = songStorage.topSongs(number)
        Response.TopSongs(top)

      case Command.CreatePlaylist(name) =>
        currentUser match {
          case Some(email) =>
            if (playlistStorage.create(name, email)) {
              Response.Success(s"Playlist '$name' created successfully")
            } else {
              Response.Error(s"Playlist '$name' already exists")
            }
          case None =>
            Response.Error("You must be logged in to create a playlist")
        }

      case Command.AddSongTo(playlistName, song) =>
        currentUser match {
          case Some(email) =>
            songStorage.getSong(song) match {
              case Some(songObj) =>
                if (playlistStorage.addSong(playlistName, email, songObj.displayName)) {
                  Response.Success(s"Song added to playlist '$playlistName'")
                } else {
                  Response.Error(s"Playlist '$playlistName' not found or song already in playlist")
                }
              case None =>
                Response.Error(s"Song '$song' not found")
            }
          case None =>
            Response.Error("You must be logged in to add songs to a playlist")
        }

      case Command.ShowPlaylist(name) =>
        currentUser match {
          case Some(email) =>
            playlistStorage.get(name, email) match {
              case Some(playlist) =>
                Response.PlaylistInfo(playlist.name, playlist.songs)
              case None =>
                Response.Error(s"Playlist '$name' not found")
            }
          case None =>
            Response.Error("You must be logged in to view a playlist")
        }

      case Command.Play(song) =>
        currentUser match {
          case Some(_) =>
            songStorage.getSong(song) match {
              case Some(songObj) =>
                songStorage.recordPlay(songObj.displayName)
                startStream(songObj)
              case None =>
                Response.Error(s"Song '$song' not found")
            }
          case None =>
            Response.Error("You must be logged in to play songs")
        }

      case Command.Stop =>
        stopActiveStream()
        Response.StreamingStopped
    }
  }

  private def startStream(song: musicapp.server.model.Song): Response = {
    stopActiveStream()

    try {
      val serverSocket = new ServerSocket(0) // ephemeral port
      val port = serverSocket.getLocalPort

      val streamer = new AudioStreamer()
      val thread = new Thread(() => {
        try {
          val streamingSocket = serverSocket.accept()
          streamer.streamSongToSocket(song, streamingSocket)
        } catch {
          case e: Exception =>
            errorLogger.logError(e, currentUser.getOrElse("anonymous"), s"Streaming song: ${song.displayName}")
        } finally {
          try serverSocket.close() catch { case _: Exception => }
        }
      })

      activeStream = Some((streamer, serverSocket, thread))
      thread.setDaemon(true)
      thread.start()

      Response.StreamingReady(port)
    } catch {
      case e: Exception =>
        errorLogger.logError(e, currentUser.getOrElse("anonymous"), s"Failed to start streaming: ${song.displayName}")
        Response.Error("Unable to start streaming right now. Try again later.")
    }
  }

  private def stopActiveStream(): Unit = {
    activeStream.foreach { case (streamer, serverSocket, thread) =>
      try streamer.stop() catch { case _: Exception => }
      try serverSocket.close() catch { case _: Exception => }
      try thread.join(500) catch { case _: Exception => }
    }
    activeStream = None
  }
}
