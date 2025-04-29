# JioSaavn Plugin For Lavalink

[![JitPack](https://jitpack.io/v/appujet/jiosaavn-plugin.svg)](https://jitpack.io/#appujet/jiosaavn-plugin)

- This is a plugin for [Lavalink](https://github.com/lavalink-devs/Lavalink)
- This plugin allows you to play songs from JioSaavn in your discord server.
- This plugin uses the [JioSaavn API](https://github.com/appujet/jiosaavn-plugin-api) to fetch songs.
- Special thanks to [topi314](https://github.com/topi314/LavaSrc) and [duncte123](https://github.com/duncte123) because most of the code for this plugin is based on [Lavasrc](https://github.com/topi314/LavaSrc) and [skybot-lavalink-plugin](https://github.com/DuncteBot/skybot-lavalink-plugin).

## Lavalink Usage

To install this plugin either download the latest release and place it into your plugins folder or add the following into your application.yml
Replace `VERSION` with the latest release version.

```yaml
lavalink:
  plugins:
    - dependency: "com.github.appujet:jiosaavn-plugin:VERSION"
      repository: "https://jitpack.io"
```

## Configuration

For all supported urls and queries see [here](#supported-urls-and-queries)

(YES `plugins` IS AT ROOT IN THE YAML)

```yaml
server: # REST and WS server
  port: 2333
  address: 0.0.0.0
lavalink:
# plugins would go here, but they are auto-loaded when developing
#  plugins:
#    - dependency: "com.github.appujet:jiosaavn-plugin:VERSION"
#      repository: "https://jitpack.io"
  server:
    password: "youshallnotpass"
    sources:
      youtube: true
      bandcamp: true
      soundcloud: true
      twitch: true
      vimeo: true
      http: true
      local: false
    bufferDurationMs: 400 # The duration of the NAS buffer. Higher values fare better against longer GC pauses
    frameBufferDurationMs: 5000 # How many milliseconds of audio to keep buffered
    youtubePlaylistLoadLimit: 6 # Number of pages at 100 each
    playerUpdateInterval: 5 # How frequently to send player updates to clients, in seconds
    youtubeSearchEnabled: true
    soundcloudSearchEnabled: true
    gc-warnings: true
    #ratelimit:
      #ipBlocks: ["1.0.0.0/8", "..."] # list of ip blocks
      #excludedIps: ["...", "..."] # ips which should be explicit excluded from usage by lavalink
      #strategy: "RotateOnBan" # RotateOnBan | LoadBalance | NanoSwitch | RotatingNanoSwitch
      #searchTriggersFail: true # Whether a search 429 should trigger marking the ip as failing
      #retryLimit: -1 # -1 = use default lavaplayer value | 0 = infinity | >0 = retry will happen this numbers times

plugins:
  jiosaavn:
    apiURL: "https://jiosaavn-plugin-api.vercel.app/api" # JioSaavn API URL
    playlistTrackLimit: 50 # The maximum number of tracks to return from given playlist (default 50 tracks)
    recommendationsTrackLimit: 10 # The maximum number of track to return from recommendations (default 10 tracks)

metrics:
  prometheus:
    enabled: false
    endpoint: /metrics

sentry:
  dsn: ""
  environment: ""
#  tags:
#    some_key: some_value
#    another_key: another_value

logging:
  file:
    max-history: 30
    max-size: 1GB
  path: ./logs/

  level:
    root: INFO
    lavalink: INFO
```

## Advantages of Using JioSaavn

- No region-based content blocking (unlike Deezer and Yandex).
- A better alternative for playing mirrored audio sources not dependent on YouTube.
- Similar content library size as Spotify and Apple Music.
- Superior to Deezer as it doesn't require any decryption key and provides slightly higher bitrate audio than Deezer's 128KBPS MP3.

## Supported URLs and Queries

### JioSaavn

- `jssearch:animals architects` - Search for a song on JioSaavn.
- `jsrec:identifier` - Get a song recommendation based on the identifier.

- <https://www.jiosaavn.com/song/apna-bana-le/ATIfejZ9bWw>
- <https://www.jiosaavn.com/album/bhediya/wSM2AOubajk>_
- <https://www.jiosaavn.com/artist/arijit-singh-songs/LlRWpHzy3Hk>_
- <https://www.jiosaavn.com/featured/jai-hanuman/8GIEhrr8clSO0eMLZZxqsA>__

## How to get API URL ?

- You can host the api locally using [this guide](https://github.com/appujet/jiosaavn-plugin-api)

---

# yt-dlp Plugin For Lavalink

[![JitPack](https://jitpack.io/v/GustavoWidman/ytdlp-plugin.svg)](https://jitpack.io/#GustavoWidman/ytdlp-plugin)

This is a plugin for [Lavalink](https://github.com/lavalink-devs/Lavalink) that uses [yt-dlp](https://github.com/yt-dlp/yt-dlp) to handle audio extraction from YouTube URLs. This plugin provides better compatibility and stability compared to the default YouTube source by:

- Automatically downloading and managing the yt-dlp binary
- Handling platform-specific binary requirements (Windows/Linux/MacOS)
- Supporting more URL formats and variations
- Working around age restrictions and region blocking
- Providing more reliable stream URLs

## Installation

To install this plugin, add the following into your `application.yml`:

```yaml
lavalink:
  plugins:
    - dependency: "com.github.gustavowidman:ytdlp-plugin:VERSION"
      repository: "https://jitpack.io"
```

Replace `VERSION` with the latest release version.

## Configuration

```yaml
plugins:
  ytdlp:
    maxRetries: 3 # Number of retries for failed downloads
    updateOnStart: true # Whether to update yt-dlp binary on startup
    binaryDirectory: null # Custom directory for yt-dlp binary, if null uses temp dir
    searchLimit: 10 # Default number of search results to return
    extractAudioOnly: true # Whether to extract audio only

# Remember to disable the default YouTube source since we're using yt-dlp
lavalink:
  server:
    sources:
      youtube: false
      youtubeSearchEnabled: false
```

## Supported URLs and Queries

The plugin supports all URLs that yt-dlp can handle, including but not limited to:

- Standard YouTube URLs: `https://www.youtube.com/watch?v=...`
- Short URLs: `https://youtu.be/...`
- Mobile URLs: `https://m.youtube.com/...`
- YouTube Music URLs: `https://music.youtube.com/...`
- YouTube Shorts: `https://www.youtube.com/shorts/...`

Search is also supported using the prefix `ytsearch:`:

```
ytsearch:name of song
```

## Features

- **Automatic Binary Management**: The plugin automatically downloads and manages the yt-dlp binary appropriate for your platform.
- **Auto-Updates**: Can automatically update the yt-dlp binary on startup to ensure compatibility.
- **Clean Cleanup**: The binary is automatically cleaned up when Lavalink shuts down.
- **Platform Support**: Works on Windows, Linux, and MacOS.
- **Configurability**: Offers various configuration options to customize behavior.
- **Search Support**: Includes YouTube search functionality.

## Dependencies

- Java 17 or higher
- A system supported by yt-dlp (Windows/Linux/MacOS)
- Internet connection for downloading the yt-dlp binary

## Building

To build the plugin:

```bash
./gradlew clean build
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
