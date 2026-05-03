package musicapp.server.model

case class Playlist(
  name: String,
  ownerEmail: String,
  songs: List[String] = List.empty
)

object Playlist {
  def fromString(line: String): Option[Playlist] = {
    val parts = line.split(",")
    if (parts.length >= 2) {
      val name = parts(0)
      val ownerEmail = parts(1)
      val songs = if (parts.length > 2) parts.drop(2).toList else List.empty
      Some(Playlist(name, ownerEmail, songs))
    } else None
  }

  def toString(playlist: Playlist): String = {
    (playlist.name :: playlist.ownerEmail :: playlist.songs).mkString(",")
  }
}
