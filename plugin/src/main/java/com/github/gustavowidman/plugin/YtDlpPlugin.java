package com.github.gustavowidman.plugin;

import com.github.gustavowidman.ytdlp.source.YtDlpAudioSourceManager;
import com.github.gustavowidman.ytdlp.source.YtDlpBinaryManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class YtDlpPlugin implements AudioPlayerManagerConfiguration {
	private static final Logger log = LoggerFactory.getLogger(YtDlpPlugin.class);

	private YtDlpBinaryManager binaryManager;
	private YtDlpAudioSourceManager sourceManager;

	public YtDlpPlugin(YtDlpConfig config) {
		log.info("Initializing yt-dlp plugin...");

		try {
			this.binaryManager = new YtDlpBinaryManager(
					config.getBinaryDirectory(),
					config.isUpdateOnStart());

			this.sourceManager = new YtDlpAudioSourceManager(binaryManager, config.getSearchLimit(),
					config.getPlaylistLimit(), config.getPlaylistThreads(),
					config.getCookiesFile(),
					config.hasCookiesFile());

			log.info("yt-dlp plugin initialized successfully!");
		} catch (Exception e) {
			log.error("Failed to initialize yt-dlp plugin", e);
		}
	}

	@Override
	public AudioPlayerManager configure(AudioPlayerManager manager) {
		if (this.sourceManager != null) {
			try {
				log.info("Registering yt-dlp source manager...");
				manager.registerSourceManager(this.sourceManager);
				log.info("yt-dlp source manager registered successfully");
			} catch (Exception e) {
				log.error("Failed to register yt-dlp source manager", e);
			}
		} else {
			log.warn("yt-dlp source manager not registered due to initialization failure");
		}
		return manager;
	}
}