package com.github.gustavowidman.ytdlp.source;

import com.github.gustavowidman.ytdlp.ExtendedAudioSourceManager;
import com.github.gustavowidman.ytdlp.ExtendedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.net.URISyntaxException;

public class YtDlpAudioTrack extends ExtendedAudioTrack {
	private final ExtendedAudioSourceManager manager;
	private long filesize;

	public YtDlpAudioTrack(AudioTrackInfo trackInfo, long filesize, ExtendedAudioSourceManager manager) {
		super(trackInfo, manager);
		this.manager = manager;
		this.filesize = filesize;
	}

	@Override
	public String getPlaybackUrl() throws URISyntaxException {
		return this.trackInfo.identifier;
	}

	@Override
	protected long getTrackDuration() {
		return this.filesize;
	}

	@Override
	protected AudioTrack makeShallowClone() {
		return new YtDlpAudioTrack(this.trackInfo, filesize, manager);
	}
}