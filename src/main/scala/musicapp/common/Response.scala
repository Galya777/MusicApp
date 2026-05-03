package musicapp.common

import java.io.{DataInputStream, DataOutputStream}

sealed trait Response

object Response {
  case class Success(message: String) extends Response
  case class Error(message: String) extends Response
  case class SearchResults(songs: List[String]) extends Response
  case class TopSongs(songs: List[(String, Int)]) extends Response
  case class PlaylistInfo(name: String, songs: List[String]) extends Response
  case class StreamingReady(port: Int) extends Response
  case object StreamingStopped extends Response
  case object Ack extends Response

  def writeToStream(response: Response, out: DataOutputStream): Unit = {
    response match {
      case Success(msg) =>
        out.writeUTF("SUCCESS")
        out.writeUTF(msg)
      case Error(msg) =>
        out.writeUTF("ERROR")
        out.writeUTF(msg)
      case SearchResults(songs) =>
        out.writeUTF("SEARCH_RESULTS")
        out.writeInt(songs.size)
        songs.foreach(out.writeUTF)
      case TopSongs(songs) =>
        out.writeUTF("TOP_SONGS")
        out.writeInt(songs.size)
        songs.foreach { case (song, count) =>
          out.writeUTF(song)
          out.writeInt(count)
        }
      case PlaylistInfo(name, songs) =>
        out.writeUTF("PLAYLIST_INFO")
        out.writeUTF(name)
        out.writeInt(songs.size)
        songs.foreach(out.writeUTF)
      case StreamingReady(port) =>
        out.writeUTF("STREAMING_READY")
        out.writeInt(port)
      case StreamingStopped =>
        out.writeUTF("STREAMING_STOPPED")
      case Ack =>
        out.writeUTF("ACK")
    }
    out.flush()
  }

  def readFromStream(in: DataInputStream): Response = {
    val responseType = in.readUTF()
    responseType match {
      case "SUCCESS" =>
        Success(in.readUTF())
      case "ERROR" =>
        Error(in.readUTF())
      case "SEARCH_RESULTS" =>
        val count = in.readInt()
        val songs = (1 to count).map(_ => in.readUTF()).toList
        SearchResults(songs)
      case "TOP_SONGS" =>
        val count = in.readInt()
        val songs = (1 to count).map { _ =>
          (in.readUTF(), in.readInt())
        }.toList
        TopSongs(songs)
      case "PLAYLIST_INFO" =>
        val name = in.readUTF()
        val count = in.readInt()
        val songs = (1 to count).map(_ => in.readUTF()).toList
        PlaylistInfo(name, songs)
      case "STREAMING_READY" =>
        StreamingReady(in.readInt())
      case "STREAMING_STOPPED" =>
        StreamingStopped
      case "ACK" =>
        Ack
      case _ =>
        Error(s"Unknown response type: $responseType")
    }
  }
}
