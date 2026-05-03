package musicapp.server.storage

import musicapp.server.model.Song
import java.io.File

class SongStorage(songsDir: String) {
  private val dir = new File(songsDir)
  
  private var songs: Map[String, Song] = loadSongs()
  private var playCounts: Map[String, Int] = Map.empty.withDefaultValue(0)

  private def loadSongs(): Map[String, Song] = {
    if (!dir.exists() || !dir.isDirectory) {
      dir.mkdirs()
      Map.empty
    } else {
      dir.listFiles()
        .filter(_.getName.endsWith(".wav"))
        .flatMap(Song.fromFile)
        .map(s => s.displayName.toLowerCase -> s)
        .toMap
    }
  }

  def getSong(name: String): Option[Song] = {
    val query = name.toLowerCase.trim
    songs.get(query).orElse {
      // allow matching by song name only (without artist)
      songs.values.find(s => s.name.toLowerCase == query)
    }.orElse {
      // allow matching by prefix of displayName before " - "
      songs.values.find(s => s.displayName.toLowerCase.startsWith(query + " - "))
    }
  }

  def search(query: String): List[Song] = {
    val lowerQuery = query.toLowerCase
    songs.values.filter { song =>
      song.name.toLowerCase.contains(lowerQuery) ||
      song.artist.toLowerCase.contains(lowerQuery)
    }.toList
  }

  def allSongs(): List[Song] = songs.values.toList

  def recordPlay(songName: String): Unit = {
    playCounts = playCounts.updated(songName, playCounts(songName) + 1)
  }

  def topSongs(n: Int): List[(String, Int)] = {
    playCounts.toList
      .sortBy(-_._2)
      .take(n)
  }

  def refresh(): Unit = {
    songs = loadSongs()
  }
}
