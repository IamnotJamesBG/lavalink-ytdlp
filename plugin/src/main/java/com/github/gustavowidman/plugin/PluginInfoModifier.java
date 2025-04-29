package com.github.gustavowidman.plugin;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.arbjerg.lavalink.api.AudioPluginInfoModifier;
import kotlinx.serialization.json.JsonElementKt;
import kotlinx.serialization.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PluginInfoModifier implements AudioPluginInfoModifier {
	@Override
	@Nullable
	public JsonObject modifyAudioPlaylistPluginInfo(@NotNull AudioPlaylist playlist) {
		return null; // No special playlist info needed for yt-dlp
	}

	@Override
	@Nullable
	public JsonObject modifyAudioTrackPluginInfo(@NotNull AudioTrack track) {
		return new JsonObject(Map.of(
				"source", JsonElementKt.JsonPrimitive("yt-dlp"),
				"identifier", JsonElementKt.JsonPrimitive(track.getIdentifier())));
	}
}