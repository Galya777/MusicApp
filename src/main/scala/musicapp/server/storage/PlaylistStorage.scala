package musicapp.server.storage

import musicapp.server.model.Playlist
import java.io.{File, PrintWriter}
import scala.io.Source

class PlaylistStorage(baseDir: String) {
  private val dir = new File(baseDir)
  if (!dir.exists()) dir.mkdirs()

  private def playlistFile(name: String, ownerEmail: String): File = {
    new File(dir, s"${ownerEmail}_$name.playlist")
  }

  def create(name: String, ownerEmail: String): Boolean = {
    val file = playlistFile(name, ownerEmail)
    if (file.exists()) {
      false
    } else {
      val playlist = Playlist(name, ownerEmail)
      save(playlist)
      true
    }
  }

  def addSong(playlistName: String, ownerEmail: String, song: String): Boolean = {
    load(playlistName, ownerEmail) match {
      case Some(playlist) =>
        if (playlist.songs.contains(song)) {
          false
        } else {
          val updated = playlist.copy(songs = playlist.songs :+ song)
          save(updated)
          true
        }
      case None => false
    }
  }

  def get(playlistName: String, ownerEmail: String): Option[Playlist] = {
    load(playlistName, ownerEmail)
  }

  def listForUser(ownerEmail: String): List[String] = {
    dir.listFiles()
      .filter(_.getName.startsWith(s"${ownerEmail}_"))
      .filter(_.getName.endsWith(".playlist"))
      .map(_.getName.drop(ownerEmail.length + 1).dropRight(9))
      .toList
  }

  private def load(name: String, ownerEmail: String): Option[Playlist] = {
    val file = playlistFile(name, ownerEmail)
    if (!file.exists()) {
      None
    } else {
      val source = Source.fromFile(file)
      try {
        val line = source.getLines().mkString("\n")
        Playlist.fromString(line)
      } finally {
        source.close()
      }
    }
  }

  private def save(playlist: Playlist): Unit = {
    val file = playlistFile(playlist.name, playlist.ownerEmail)
    val writer = new PrintWriter(file)
    try {
      writer.println(Playlist.toString(playlist))
    } finally {
      writer.close()
    }
  }
}
