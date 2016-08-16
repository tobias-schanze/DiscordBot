package novaz.handler;

import novaz.db.model.OMusic;
import novaz.db.table.TMusic;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MusicPlayerHandler {
	private final static Map<IGuild, MusicPlayerHandler> playerInstances = new ConcurrentHashMap<>();
	private final IGuild guild;
	private final NovaBot bot;
	private IMessage activeMsg;

	public static MusicPlayerHandler getAudioPlayerForGuild(IGuild guild, NovaBot bot) {
		if (playerInstances.containsKey(guild)) {
			return playerInstances.get(guild);
		} else {
			return new MusicPlayerHandler(guild, bot);
		}
	}

	/**
	 * Skips currently playing song
	 */
	public void skipSong() {
		clearMessage();
		AudioPlayer ap = AudioPlayer.getAudioPlayerForGuild(guild);
		ap.skip();
		if (ap.getPlaylistSize() == 0) {
			playRandomSong();
		}
	}

	private MusicPlayerHandler(IGuild guild, NovaBot bot) {
		this.guild = guild;
		this.bot = bot;
		playerInstances.put(guild, this);
	}

	/**
	 * retreives a random .mp3 file from the music directory
	 *
	 * @return filename
	 * @todo make it less random
	 */
	private String getRandomSong() {
		File folder = new File(Config.MUSIC_DIRECTORY);
		String[] fileList = folder.list((dir, name) -> name.toLowerCase().endsWith(".mp3"));
		return fileList[(int) (Math.random() * (double) fileList.length)];
	}

	/**
	 * A track has ended
	 *
	 * @param oldTrack  track which just stopped
	 * @param nextTrack next track
	 */
	public void onTrackEnded(AudioPlayer.Track oldTrack, Optional<AudioPlayer.Track> nextTrack) {
		clearMessage();
		if (!nextTrack.isPresent()) {
			playRandomSong();
		}
	}

	/**
	 * a track has started
	 *
	 * @param track the track which has started
	 */
	public void onTrackStarted(AudioPlayer.Track track) {
		clearMessage();
		Map<String, Object> metadata = track.getMetadata();
		String msg = "Now playing unknown file :(";
		if (metadata.containsKey("file")) {
			if (metadata.get("file") instanceof File) {
				File f = (File) metadata.get("file");
				OMusic music = TMusic.findByFileName(f.getName());
				if (music.title.isEmpty()) {
					msg = "plz send help:: " + f.getName();
				} else {
					msg = "Now playing " + music.title;
				}
			}
		}
		activeMsg = bot.sendMessage(guild.getChannels().get(0), msg);
	}

	/**
	 * Deletes 'now playing' message if it exists
	 */
	private void clearMessage() {
		if (activeMsg != null) {
			try {
				activeMsg.delete();
				activeMsg = null;
			} catch (MissingPermissionsException | RateLimitException | DiscordException ignored) {
			}
		}
	}

	/**
	 * Adds a random song from the music directory to the queue
	 */
	public void playRandomSong() {
		String randomSong = getRandomSong();
		try {
			AudioPlayer.getAudioPlayerForGuild(guild).queue(new File(Config.MUSIC_DIRECTORY + randomSong));
		} catch (IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Clears existing message and stops playing music for guild
	 */
	public void stopMusic() {
		clearMessage();
		AudioPlayer.getAudioPlayerForGuild(guild).clear();
	}
}