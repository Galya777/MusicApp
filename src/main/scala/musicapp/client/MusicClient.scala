package musicapp.client

import musicapp.client.network.ServerConnection

object MusicClient {
  private val DEFAULT_HOST = "localhost"
  private val DEFAULT_PORT = 8888

  def main(args: Array[String]): Unit = {
    val host = if (args.length > 0) args(0) else DEFAULT_HOST
    val port = if (args.length > 1) args(1).toInt else DEFAULT_PORT

    val connection = new ServerConnection(host, port)

    if (connection.connect()) {
      val cli = new CliInterface(connection)
      cli.start()
    } else {
      println("Failed to connect to the server.")
      println("Please check:")
      println("  - Is the server running?")
      println("  - Is the host and port correct?")
      println("  - Is there a network connectivity issue?")
    }
  }
}
