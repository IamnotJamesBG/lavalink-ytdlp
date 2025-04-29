package com.github.gustavowidman.ytdlp.source;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.*;

public class YtDlpBinaryManager {
	private static final Logger log = LoggerFactory.getLogger(YtDlpBinaryManager.class);
	private static final String YT_DLP_RELEASES = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/";
	private final Path binaryPath;

	public YtDlpBinaryManager(String customDir, boolean updateOnStart) {
		try {
			// Determine binary name based on OS
			String binaryName;
			switch (getOS()) {
				case WINDOWS:
					binaryName = "yt-dlp.exe";
					break;
				case MAC:
				case LINUX:
					binaryName = "yt-dlp";
					break;
				default:
					throw new FriendlyException("Unsupported operating system",
							FriendlyException.Severity.FAULT, null);
			}

			// Set up binary path
			if (customDir != null) {
				Path dirPath = Paths.get(customDir);
				// Create directory if it doesn't exist
				Files.createDirectories(dirPath);
				binaryPath = dirPath.resolve(binaryName);
				log.info("Using custom directory for yt-dlp binary: {}", dirPath);
			} else {
				binaryPath = Files.createTempDirectory("ytdlp").resolve(binaryName);
				// Ensure temp directory is deleted on JVM exit
				binaryPath.toFile().deleteOnExit();
				binaryPath.getParent().toFile().deleteOnExit();
				log.info("Using temporary directory for yt-dlp binary: {}", binaryPath.getParent());
			}

			// Download or update binary
			if (updateOnStart || !Files.exists(binaryPath)) {
				downloadBinary();
			}

		} catch (IOException e) {
			throw new FriendlyException("Failed to initialize yt-dlp binary",
					FriendlyException.Severity.FAULT, e);
		}
	}

	private void downloadBinary() throws IOException {
		String downloadUrl = YT_DLP_RELEASES + binaryPath.getFileName().toString();
		log.info("Downloading yt-dlp from: {}", downloadUrl);

		try {
			URI uri = URI.create(downloadUrl);
			try (InputStream in = uri.toURL().openStream()) {
				Files.copy(in, binaryPath, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			throw new IOException("Failed to download yt-dlp binary", e);
		}

		// Make binary executable on Unix systems
		if (getOS() != OS.WINDOWS) {
			binaryPath.toFile().setExecutable(true);
		}
	}

	public Path getBinaryPath() {
		return binaryPath;
	}

	private enum OS {
		WINDOWS, MAC, LINUX, UNKNOWN
	}

	private OS getOS() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win"))
			return OS.WINDOWS;
		if (os.contains("mac"))
			return OS.MAC;
		if (os.contains("nix") || os.contains("nux"))
			return OS.LINUX;
		return OS.UNKNOWN;
	}
}