package musicapp.server.model

case class Song(
  name: String,
  artist: String,
  filePath: String
) {
  def displayName: String = s"$name - $artist"
}

object Song {
  def fromFile(file: java.io.File): Option[Song] = {
    val name = file.getName.replace(".wav", "")
    // Assume format: "SongName - Artist.wav" or just "SongName.wav"
    val parts = name.split(" - ", 2)
    if (parts.length == 2) {
      Some(Song(parts(0).trim, parts(1).trim, file.getAbsolutePath))
    } else {
      Some(Song(name, "Unknown Artist", file.getAbsolutePath))
    }
  }
}
