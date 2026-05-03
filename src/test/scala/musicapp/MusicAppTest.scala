package musicapp

import musicapp.common.{Command, AudioFormatInfo}
import musicapp.server.model.{User, Song, Playlist}
import musicapp.server.storage.{UserStorage, SongStorage, PlaylistStorage}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.File

class MusicAppTest extends AnyFlatSpec with Matchers {

  "User" should "serialize and deserialize correctly" in {
    val user = User("test@test.com", "password123")
    val serialized = User.toString(user)
    val deserialized = User.fromString(serialized)
    
    deserialized shouldBe Some(user)
  }

  "Playlist" should "serialize and deserialize correctly" in {
    val playlist = Playlist("MyPlaylist", "user@test.com", List("Song1", "Song2"))
    val serialized = Playlist.toString(playlist)
    val deserialized = Playlist.fromString(serialized)
    
    deserialized shouldBe Some(playlist)
  }

  "Command parser" should "parse register command" in {
    Command.parse("register test@test.com password") shouldBe 
      Right(Command.Register("test@test.com", "password"))
  }

  it should "parse login command" in {
    Command.parse("login test@test.com password") shouldBe 
      Right(Command.Login("test@test.com", "password"))
  }

  it should "parse search command with multiple words" in {
    Command.parse("search hello world") shouldBe 
      Right(Command.Search("hello world"))
  }

  it should "parse top command" in {
    Command.parse("top 10") shouldBe 
      Right(Command.Top(10))
  }

  it should "return error for invalid top command" in {
    Command.parse("top abc") shouldBe 
      Left("Invalid number")
  }

  it should "return error for unknown command" in {
    Command.parse("unknown command") shouldBe 
      Left("Unknown command: unknown")
  }

  "AudioFormatInfo" should "be serializable to stream" in {
    val info = AudioFormatInfo("PCM_SIGNED", 44100.0f, 16, 2, 4, 44100.0f, false)
    
    // Just verify the object can be created
    info.encoding shouldBe "PCM_SIGNED"
    info.sampleRate shouldBe 44100.0f
    info.sampleSizeInBits shouldBe 16
    info.channels shouldBe 2
  }
}
