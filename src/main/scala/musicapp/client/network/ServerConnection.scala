package musicapp.client.network

import musicapp.common.{AudioFormatInfo, Command, Response}
import musicapp.client.audio.AudioPlayer

import java.io.{DataInputStream, DataOutputStream, IOException}
import java.net.{Socket, SocketException}

class ServerConnection(host: String, port: Int) {
  private var socket: Option[Socket] = None
  private var input: Option[DataInputStream] = None
  private var output: Option[DataOutputStream] = None
  private val audioPlayer = new AudioPlayer()
  @volatile private var streaming = false
  private var streamingSocket: Option[Socket] = None
  private var streamingThread: Option[Thread] = None

  def connect(): Boolean = {
    try {
      val s = new Socket(host, port)
      socket = Some(s)
      input = Some(new DataInputStream(s.getInputStream))
      output = Some(new DataOutputStream(s.getOutputStream))
      true
    } catch {
      case e: IOException =>
        println(s"Unable to connect to server at $host:$port")
        println("Please ensure the server is running and try again.")
        false
    }
  }

  def disconnect(): Unit = {
    stopStreaming()
    try {
      sendCommand(Command.Disconnect)
    } catch {
      case _: Exception => // Ignore disconnect errors
    }
    input.foreach(_.close())
    output.foreach(_.close())
    socket.foreach(_.close())
    socket = None
    input = None
    output = None
  }

  def sendCommand(command: Command): Response = {
    try {
      output.foreach { out =>
        out.writeUTF(commandToString(command))
        out.flush()
      }

      val response = input.map(Response.readFromStream).getOrElse(
        Response.Error("Not connected to server")
      )

      // Handle streaming response
      command match {
        case Command.Play(_) =>
          response match {
            case Response.StreamingReady(streamPort) =>
              startStreaming(streamPort)
            case _ =>
          }
        case _ =>
      }

      response
    } catch {
      case e: SocketException =>
        Response.Error("Connection lost. Please reconnect.")
      case e: IOException =>
        Response.Error("Network error occurred. Please try again.")
    }
  }

  private def startStreaming(streamPort: Int): Unit = {
    stopStreaming()
    streaming = true

    val s = new Socket(host, streamPort)
    streamingSocket = Some(s)
    val streamInput = new DataInputStream(s.getInputStream)

    val thread = new Thread(() => {
      try {
        val formatInfo = AudioFormatInfo.readFromStream(streamInput)
        audioPlayer.init(formatInfo)

        val buffer = new Array[Byte](8192)
        var lastRead = 0

        while (streaming && lastRead != -1) {
          lastRead = streamInput.read(buffer)
          if (lastRead > 0) {
            audioPlayer.write(buffer, 0, lastRead)
          }
        }
      } catch {
        case _: SocketException =>
          // Connection closed or interrupted
        case _: IOException =>
          // Stream ended
        case e: Exception =>
          println(s"Streaming error: ${e.getMessage}")
      } finally {
        try streamInput.close() catch { case _: Exception => }
        try s.close() catch { case _: Exception => }
        streamingSocket = None
        audioPlayer.stop()
        streaming = false
      }
    })

    streamingThread = Some(thread)
    thread.setDaemon(true)
    thread.start()
  }

  def stopStreaming(): Unit = {
    streaming = false
    streamingSocket.foreach { s =>
      try s.close() catch { case _: Exception => }
    }
    streamingSocket = None
    streamingThread.foreach { t =>
      try t.join(200) catch { case _: Exception => }
    }
    streamingThread = None
    audioPlayer.stop()
  }

  private def commandToString(command: Command): String = {
    command match {
      case Command.Register(email, password) => s"register $email $password"
      case Command.Login(email, password) => s"login $email $password"
      case Command.Disconnect => "disconnect"
      case Command.Search(words) => s"search $words"
      case Command.Top(n) => s"top $n"
      case Command.CreatePlaylist(name) => s"create-playlist $name"
      case Command.AddSongTo(playlist, song) => s"add-song-to $playlist $song"
      case Command.ShowPlaylist(name) => s"show-playlist $name"
      case Command.Play(song) => s"play $song"
      case Command.Stop => "stop"
    }
  }
}
