import java.io.*
import java.net.*
import musicapp.common.*

object IntegrationTest {
  def main(args: Array[String]): Unit = {
    val port = 8888
    val host = "localhost"

    try {
      val socket = new Socket(host, port)
      val out = new DataOutputStream(socket.getOutputStream())
      val in = new DataInputStream(socket.getInputStream())

      println("[TEST] Connecting to server...")

      // 1. Test Registration
      println("[TEST] Testing registration...")
      out.writeUTF("register testuser@fmi.bg pass123")
      out.flush()
      val regResponse = Response.readFromStream(in)
      println(s"[TEST] Registration response: $regResponse")

      // 2. Test Login
      println("[TEST] Testing login...")
      out.writeUTF("login testuser@fmi.bg pass123")
      out.flush()
      val loginResponse = Response.readFromStream(in)
      println(s"[TEST] Login response: $loginResponse")

      // 3. Test Search
      println("[TEST] Testing search...")
      out.writeUTF("search atlantic")
      out.flush()
      val searchResponse = Response.readFromStream(in)
      println(s"[TEST] Search response: $searchResponse")

      // 4. Test Play
      println("[TEST] Testing play...")
      out.writeUTF("play atlanticlights-atlantic-lights-jupiter-402615")
      out.flush()
      val playResponse = Response.readFromStream(in)
      println(s"[TEST] Play response: $playResponse")

      playResponse match {
        case Response.StreamingReady(streamingPort) =>
          println(s"[TEST] Streaming ready on port $streamingPort. Connecting...")
          val streamSocket = new Socket(host, streamingPort)
          val streamIn = new DataInputStream(streamSocket.getInputStream)
          
          // Should receive AudioFormatInfo first
          val format = AudioFormatInfo.readFromStream(streamIn)
          println(s"[TEST] Stream format received: $format")
          
          streamSocket.close()
          println("[TEST] Stream connection test successful")
        case _ =>
          println("[TEST] Play command did not return StreamingReady")
      }

      // 4. Test Top Songs
      println("[TEST] Testing top songs...")
      out.writeUTF("top 5")
      out.flush()
      val topResponse = Response.readFromStream(in)
      println(s"[TEST] Top songs response: $topResponse")

      // 5. Test Playlist creation
      println("[TEST] Testing playlist creation...")
      out.writeUTF("create-playlist FMI-Demo")
      out.flush()
      val playlistResponse = Response.readFromStream(in)
      println(s"[TEST] Playlist creation response: $playlistResponse")

      // 6. Test Disconnect
      println("[TEST] Testing disconnect...")
      out.writeUTF("disconnect")
      out.flush()
      val disconnectResponse = Response.readFromStream(in)
      println(s"[TEST] Disconnect response: $disconnectResponse")

      socket.close()
      println("[TEST] Integration test finished.")
      System.exit(0)
    } catch {
      case e: Exception =>
        println(s"[TEST] Error: ${e.getMessage}")
        e.printStackTrace()
        System.exit(1)
    }
  }
}
