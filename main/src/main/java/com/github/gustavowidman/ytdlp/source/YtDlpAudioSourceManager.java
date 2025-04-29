package com.github.gustavowidman.ytdlp.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.gustavowidman.ytdlp.ExtendedAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class YtDlpAudioSourceManager extends ExtendedAudioSourceManager {
	private static final Logger log = LoggerFactory.getLogger(YtDlpAudioSourceManager.class);

	private static final String SEARCH_PREFIX = "ytsearch:";
	private static final Pattern VIDEO_PATTERN = Pattern.compile(
			"^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/shorts/)[a-zA-Z0-9_-]+.*$");
	private static final Pattern PLAYLIST_PATTERN = Pattern
			.compile("^(https?://)?(www\\.)?youtube\\.com/playlist\\?list=[a-zA-Z0-9_-]+.*$");

	private final YtDlpBinaryManager binaryManager;
	private final int searchLimit;
	private final int playlistLimit;
	private final int playlistThreads;
	private final String cookiesFile;
	private final boolean hasCookiesFile;

	public YtDlpAudioSourceManager(
			YtDlpBinaryManager binaryManager,
			int searchLimit,
			int playlistLimit,
			int playlistThreads,
			String cookiesFile,
			boolean hasCookiesFile) {
		this.binaryManager = binaryManager;
		this.cookiesFile = cookiesFile;
		this.searchLimit = searchLimit;
		this.hasCookiesFile = hasCookiesFile;
		this.playlistLimit = playlistLimit;
		this.playlistThreads = playlistThreads;

		log.info("YtDlp source manager initialized with search limit: {}", searchLimit);
	}

	@Override
	public String getSourceName() {
		return "youtube";
	}

	@Override
	public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
		String identifier = reference.identifier;
		log.debug("Loading item with identifier: {}", identifier);

		if (identifier.startsWith(SEARCH_PREFIX)) {
			return loadSearch(identifier.substring(SEARCH_PREFIX.length()));
		}

		try {
			if (VIDEO_PATTERN.matcher(identifier).matches() || identifier.contains("youtube.com/watch?v=")
					|| identifier.contains("youtu.be/")) {
				log.debug("URL matches video pattern, loading track...");
				return loadTrack(identifier);
			} else if (PLAYLIST_PATTERN.matcher(identifier).matches() || identifier.contains("youtube.com/playlist?")) {
				log.debug("URL matches playlist pattern, loading playlist...");
				return loadPlaylist(identifier);
			}
		} catch (Exception e) {
			log.error("Error loading item: {}", e.getMessage(), e);
			throw new FriendlyException(
					"Error loading YouTube item: " + e.getMessage(),
					FriendlyException.Severity.FAULT,
					e);
		}

		return null;
	}

	private YtDlpAudioTrack loadTrack(String url)
			throws Exception {
		log.debug("Loading track metadata for URL: {}", url);
		ProcessBuilder pb = new ProcessBuilder();
		pb.command(
				binaryManager.getBinaryPath().toString(),
				"--no-warnings",
				"--skip-download",
				"--print",
				"{\"duration\": %(duration)s, \"author\": \"%(uploader)s\", \"title\": \"%(title)s\", \"url\": \"%(url)s\", \"thumbnail\": \"%(thumbnail)s\", \"filesize\": %(filesize)s}",
				"--no-playlist",
				"--quiet",
				"--no-check-certificate",
				"--geo-bypass",
				"--ignore-errors",
				"--format", "ba");

		if (!hasCookiesFile) {
			log.warn("No cookies file provided. Some videos may not be accessible.");
		} else {
			Path cookiePath = Paths.get(cookiesFile);
			// Create directory if it doesn't exist
			Files.createDirectories(cookiePath.getParent());

			pb.command().addAll(List.of("--cookies", cookiePath.toString()));
		}

		pb.command().add(url);

		pb.redirectErrorStream(true);
		Process process = pb.start();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			StringBuilder output = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line);
			}

			if (!process.waitFor(30, TimeUnit.SECONDS)) {
				process.destroy();
				throw new FriendlyException("yt-dlp process timed out",
						FriendlyException.Severity.FAULT, null);
			}

			if (process.exitValue() != 0) {
				throw new FriendlyException("yt-dlp failed: " + output,
						FriendlyException.Severity.FAULT, null);
			}

			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			com.fasterxml.jackson.databind.JsonNode videoInfo = mapper.readTree(output.toString());

			String title = videoInfo.get("title").asText();
			String author = videoInfo.get("author").asText();
			String thumbnailUrl = videoInfo.get("thumbnail").asText();
			String playbackUrl = videoInfo.get("url").asText();
			long filesize = videoInfo.get("filesize").asLong();
			long duration = (long) (videoInfo.get("duration").asDouble() * 1000);

			log.debug("Successfully loaded track metadata: {} by {}", title, author);

			AudioTrackInfo trackInfo = new AudioTrackInfo(
					title,
					author,
					duration,
					playbackUrl,
					false,
					url,
					thumbnailUrl,
					null);

			return new YtDlpAudioTrack(trackInfo, filesize, this);
		}
	}

	private AudioItem loadPlaylist(String url) throws Exception {
		log.debug("Loading playlist sub-urls for playlist URL: {}", url);

		ProcessBuilder pb = new ProcessBuilder();
		pb.command(
				binaryManager.getBinaryPath().toString(),
				"--no-warnings",
				"--skip-download",
				"--dump-single-json",
				"--yes-playlist",
				"--quiet",
				"--no-check-certificate",
				"--geo-bypass",
				"--ignore-errors",
				"--flat-playlist",
				"--format", "ba[acodec=opus]");

		if (!hasCookiesFile) {
			log.warn("No cookies file provided. Some videos may not be accessible.");
		} else {
			Path cookiePath = Paths.get(cookiesFile);
			// Create directory if it doesn't exist
			Files.createDirectories(cookiePath.getParent());

			pb.command().addAll(List.of("--cookies", cookiePath.toString()));
		}

		pb.command().add(url);

		pb.redirectErrorStream(true);
		Process process = pb.start();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			StringBuilder output = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line);
			}

			if (!process.waitFor(30, TimeUnit.SECONDS)) {
				process.destroy();
				throw new FriendlyException("yt-dlp process timed out",
						FriendlyException.Severity.FAULT, null);
			}

			if (process.exitValue() != 0) {
				throw new FriendlyException("yt-dlp failed: " + output,
						FriendlyException.Severity.FAULT, null);
			}

			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			com.fasterxml.jackson.databind.JsonNode playlistInfo = mapper.readTree(output.toString());

			List<AudioTrack> tracks = new java.util.ArrayList<>();

			ExecutorService executor = Executors.newFixedThreadPool(playlistThreads);
			List<Future<YtDlpAudioTrack>> futures = new java.util.ArrayList<>();

			for (JsonNode trackData : playlistInfo.get("entries")) {
				String trackUrl = trackData.get("url").asText();
				futures.add(executor.submit(() -> loadTrack(trackUrl)));

				if (futures.size() >= playlistLimit) {
					log.warn("Reached playlist limit of {} tracks, stopping further loading.", playlistLimit);
					break;
				}
			}

			for (Future<YtDlpAudioTrack> future : futures) {
				try {
					YtDlpAudioTrack track = future.get(30, TimeUnit.SECONDS);
					if (track != null) {
						tracks.add(track);
					}
				} catch (Exception e) {
					log.warn("Failed to load track: {}", e.getMessage());
				}
			}

			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.MINUTES);

			return new BasicAudioPlaylist(
					playlistInfo.get("title").asText(),
					tracks,
					null,
					false);
		}
	}

	private AudioItem loadSearch(String query) {
		log.debug("Searching for: {} (limit: {})", query, searchLimit);
		// Search loading will be implemented later
		return null;
	}

	@Override
	public boolean isTrackEncodable(AudioTrack track) {
		return false;
	}

	@Override
	public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
		// no-op
	}

	@Override
	public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input)
			throws IOException {
		return new YtDlpAudioTrack(trackInfo, Units.CONTENT_LENGTH_UNKNOWN, this);
	}

	public YtDlpBinaryManager getBinaryManager() {
		return binaryManager;
	}
}