package com.github.gustavowidman.plugin;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "plugins.ytdlp")
public class YtDlpConfig {
	private boolean updateOnStart = true;
	private String binaryDirectory = null;
	private String cookiesFile = null;
	private int searchLimit = 10;
	private int playlistLimit = 10;
	private int playlistThreads = 8;

	public boolean isUpdateOnStart() {
		return updateOnStart;
	}

	public void setUpdateOnStart(boolean updateOnStart) {
		this.updateOnStart = updateOnStart;
	}

	public String getBinaryDirectory() {
		return binaryDirectory;
	}

	public void setBinaryDirectory(String binaryDirectory) {
		this.binaryDirectory = binaryDirectory;
	}

	public int getSearchLimit() {
		return searchLimit;
	}

	public void setSearchLimit(int searchLimit) {
		this.searchLimit = searchLimit;
	}

	public String getCookiesFile() {
		return cookiesFile;
	}

	public void setCookiesFile(String cookiesFile) {
		this.cookiesFile = cookiesFile;
	}

	public boolean hasCookiesFile() {
		return cookiesFile != null && !cookiesFile.isEmpty();
	}

	public int getPlaylistLimit() {
		return playlistLimit;
	}

	public void setPlaylistLimit(int playlistLimit) {
		this.playlistLimit = playlistLimit;
	}

	public int getPlaylistThreads() {
		return playlistThreads;
	}

	public void setPlaylistThreads(int playlistThreads) {
		this.playlistThreads = playlistThreads;
	}
}