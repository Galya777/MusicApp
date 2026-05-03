package musicapp.server.audio

import musicapp.common.AudioFormatInfo
import musicapp.server.model.Song
import java.io.{DataOutputStream, File}
import java.net.Socket
import javax.sound.sampled.AudioSystem

class AudioStreamer {
  @volatile private var shouldStop = false

  def stop(): Unit = {
    shouldStop = true
  }

  def streamSongToSocket(song: Song, socket: Socket): Unit = {
    val out = new DataOutputStream(socket.getOutputStream)
    try {
      val audioInputStream = AudioSystem.getAudioInputStream(new File(song.filePath))
      try {
        val formatInfo = AudioFormatInfo.fromAudioFormat(audioInputStream.getFormat)

        // Send format info first
        AudioFormatInfo.writeToStream(formatInfo, out)
        out.flush()

        val buffer = new Array[Byte](8192)
        var bytesRead = 0

        while (!shouldStop && bytesRead != -1) {
          bytesRead = audioInputStream.read(buffer)
          if (bytesRead > 0) {
            out.write(buffer, 0, bytesRead)
            out.flush()
          }
        }
      } finally {
        audioInputStream.close()
      }
    } finally {
      socket.close()
    }
  }
}
