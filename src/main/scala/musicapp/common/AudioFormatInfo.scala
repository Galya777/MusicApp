package musicapp.common

import java.io.{DataInputStream, DataOutputStream}
import javax.sound.sampled.AudioFormat

case class AudioFormatInfo(
  encoding: String,
  sampleRate: Float,
  sampleSizeInBits: Int,
  channels: Int,
  frameSize: Int,
  frameRate: Float,
  bigEndian: Boolean
)

object AudioFormatInfo {
  def fromAudioFormat(format: AudioFormat): AudioFormatInfo = {
    AudioFormatInfo(
      encoding = format.getEncoding.toString,
      sampleRate = format.getSampleRate,
      sampleSizeInBits = format.getSampleSizeInBits,
      channels = format.getChannels,
      frameSize = format.getFrameSize,
      frameRate = format.getFrameRate,
      bigEndian = format.isBigEndian
    )
  }

  def toAudioFormat(info: AudioFormatInfo): AudioFormat = {
    val encoding = new AudioFormat.Encoding(info.encoding)
    new AudioFormat(
      encoding,
      info.sampleRate,
      info.sampleSizeInBits,
      info.channels,
      info.frameSize,
      info.frameRate,
      info.bigEndian
    )
  }

  def writeToStream(info: AudioFormatInfo, out: DataOutputStream): Unit = {
    out.writeUTF(info.encoding)
    out.writeFloat(info.sampleRate)
    out.writeInt(info.sampleSizeInBits)
    out.writeInt(info.channels)
    out.writeInt(info.frameSize)
    out.writeFloat(info.frameRate)
    out.writeBoolean(info.bigEndian)
  }

  def readFromStream(in: DataInputStream): AudioFormatInfo = {
    AudioFormatInfo(
      encoding = in.readUTF(),
      sampleRate = in.readFloat(),
      sampleSizeInBits = in.readInt(),
      channels = in.readInt(),
      frameSize = in.readInt(),
      frameRate = in.readFloat(),
      bigEndian = in.readBoolean()
    )
  }
}
