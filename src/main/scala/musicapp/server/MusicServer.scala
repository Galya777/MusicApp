package musicapp.server

import musicapp.server.logging.ErrorLogger
import musicapp.server.storage.{PlaylistStorage, SongStorage, UserStorage}

import java.net.{ServerSocket, Socket}
import java.util.concurrent.Executors

object MusicServer {
  private val PORT = 8888
  private val THREAD_POOL_SIZE = 10

  def main(args: Array[String]): Unit = {
    val errorLogger = new ErrorLogger("server_errors.log")
    val userStorage = new UserStorage("users/users.txt")
    val songStorage = new SongStorage("songs")
    val playlistStorage = new PlaylistStorage("playlists")

    val executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)
    var serverSocket: ServerSocket = null

    try {
      serverSocket = new ServerSocket(PORT)
      println(s"Music Server started on port $PORT")
      println(s"Place .wav files in the 'songs' directory")
      println("Press Ctrl+C to stop the server")

      while (true) {
        try {
          val clientSocket = serverSocket.accept()
          println(s"Client connected: ${clientSocket.getInetAddress}")

          val handler = new ClientHandler(
            clientSocket,
            userStorage,
            songStorage,
            playlistStorage,
            errorLogger
          )
          executor.execute(handler)
        } catch {
          case e: Exception =>
            errorLogger.logError(e, "server", "Error accepting client connection")
        }
      }
    } catch {
      case e: Exception =>
        errorLogger.logError(e, "server", "Server initialization error")
        println(s"Server error: ${e.getMessage}")
    } finally {
      executor.shutdown()
      if (serverSocket != null && !serverSocket.isClosed) {
        serverSocket.close()
      }
    }
  }
}
