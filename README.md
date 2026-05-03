# Music App - Spotify-like Music Streaming Application

A music streaming application built in Scala, consisting of a server and client component that allows users to stream music, create playlists, and manage their music library.

## Project Structure

```
MusicApp/
├── src/main/scala/musicapp/
│   ├── server/           # Server-side code
│   │   ├── audio/          # Audio streaming
│   │   ├── logging/        # Error logging
│   │   ├── model/          # Data models (User, Song, Playlist)
│   │   ├── storage/        # File persistence
│   │   ├── ClientHandler.scala
│   │   └── MusicServer.scala
│   ├── client/             # Client-side code
│   │   ├── audio/          # Audio playback
│   │   ├── network/        # Server connection
│   │   ├── CliInterface.scala
│   │   └── MusicClient.scala
│   └── common/             # Shared code
│       ├── AudioFormatInfo.scala
│       ├── Command.scala
│       └── Response.scala
├── songs/                  # Place .wav files here
├── playlists/              # Playlists storage directory
├── users/                  # User data storage directory
└── build.sbt               # SBT build configuration
```

## Requirements

- Java 11 or higher
- Scala 3.3.1 or higher
- SBT 1.9 or higher (if you run from terminal)
- .wav format audio files for the music library

## Running the Application

### 1. Start the Server

```bash
sbt "runMain musicapp.server.MusicServer"
```

The server will start on port 8888. Place your .wav audio files in the `songs/` directory.

### 2. Start the Client

In a new terminal:

```bash
sbt "runMain musicapp.client.MusicClient"
```

Or connect to a remote server:

```bash
sbt "runMain musicapp.client.MusicClient localhost 8888"
```

## Available Commands

### Authentication
- `register <email> <password>` - Create a new account
- `login <email> <password>` - Login to your account
- `disconnect` - Logout from your account

### Music Discovery
- `search <words>` - Search for songs by name or artist
- `top <number>` - Show the top N most played songs

### Playlists (requires login)
- `create-playlist <name>` - Create a new playlist
- `add-song-to <playlist> <song>` - Add a song to a playlist
- `show-playlist <name>` - Display playlist contents

### Playback (requires login)
- `play <song>` - Stream and play a song
- `stop` - Stop playback

### General
- `help` - Show available commands
- `exit` or `quit` - Close the application

## Audio Format

This application uses `javax.sound.sampled` and only supports `.wav` files. The server streams audio data using `SourceDataLine` to the client, which allows real-time playback without loading the entire file into memory.

Streaming is implemented over a dedicated TCP connection:

- The client sends `play <song>` over the command connection.
- The server responds with a `STREAMING_READY` response containing a TCP port.
- The client opens a new TCP connection to that port and receives:
  1) audio format info (`AudioFormatInfo`)
  2) raw audio bytes

## Storage

- **Users**: Stored in `users/users.txt`
- **Playlists**: Stored in `playlists/<email>_<playlist_name>.playlist`
- **Songs**: Place .wav files in the `songs/` directory
- **Error Logs**: `server_errors.log` (server), `client_errors.log` (client)

## Error Handling

The application includes comprehensive error handling:
- User-friendly error messages in the console
- Detailed error logs with stack traces written to log files
- Graceful handling of network issues and disconnections

## Building and Packaging

To create a standalone JAR:

```bash
sbt assembly
```

Or compile all classes:

```bash
sbt compile
```

## Notes

- Song files should follow the naming convention: `SongName - Artist.wav` for proper display
- The server must have the requested song file in the `songs/` directory
- Audio streaming requires a stable network connection
