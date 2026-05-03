package musicapp.client.audio

import musicapp.common.AudioFormatInfo
import musicapp.common.AudioFormatInfo.toAudioFormat
import javax.sound.sampled.{DataLine, SourceDataLine}

class AudioPlayer {
  private var dataLine: Option[SourceDataLine] = None
  private var playing = false

  def init(formatInfo: AudioFormatInfo): Unit = {
    try {
      val format = toAudioFormat(formatInfo)
      val info = new DataLine.Info(classOf[SourceDataLine], format)
      val line = javax.sound.sampled.AudioSystem.getLine(info).asInstanceOf[SourceDataLine]
      line.open(format)
      line.start()
      dataLine = Some(line)
      playing = true
      println("Audio player initialized. Playing...")
    } catch {
      case e: Exception =>
        println(s"Error initializing audio: ${e.getMessage}")
        throw e
    }
  }

  def write(buffer: Array[Byte], offset: Int, length: Int): Unit = {
    dataLine.foreach(_.write(buffer, offset, length))
  }

  def stop(): Unit = {
    playing = false
    dataLine.foreach { line =>
      line.stop()
      line.close()
    }
    dataLine = None
    println("Playback stopped")
  }

  def isPlaying: Boolean = playing
}
