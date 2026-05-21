package boogle.sound;

/**
 * Utility class providing convenient access to various sound effects and
 * background music used in the Boogle game. All methods return a
 * {@link Clip} that has already been opened and, in the case of looping
 * tracks, begun playback. The underlying audio data is loaded from the
 * {@code asset} subdirectory on the classpath. If a sound cannot be
 * located or fails to load the methods will print an error message to
 * standard error and return {@code null}. This class maintains a
 * {@link Random} to choose between multiple in‑game tracks and offers a
 * simple flag to globally mute audio (although the flag is not currently
 * checked). It is intended to be used statically.
 */

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Random;

public final class GameSound {

    /** Random instance used to select one of the in‑game music tracks. */
    private static final Random RANDOM = new Random();

    /**
     * Array of file names for music clips played during game turns. A random
     * element of this array is selected each time {@link #ingame()} is
     * invoked.
     */
    private static final String[] INGAME_TRACKS = {
            "asset/ingame1.wav",
            "asset/ingame2.wav",
            "asset/ingame3.wav",
            "asset/ingame4.wav"
    };

    /**
     * Global mute flag. When {@code true} audio will not be played. This
     * flag is currently not checked by the playback methods but is exposed
     * for potential future use.
     */
    private static boolean muted = false;

    /**
     * Plays the negative feedback sound. There is approximately a 2%
     * probability that an alternate “bad” sound will be chosen instead of
     * the default clip. The returned clip is started immediately and will
     * play once.
     *
     * @return a {@link Clip} playing a negative sound effect, or {@code null}
     *         if the audio resource is unavailable
     */
    public static Clip bad() {
        // 1 in 50 chance to FAHHH
        if (Math.random() > 0.98) {
            return play("asset/bad-alt.wav", false);
        }

        return play("asset/bad.wav", false);
    }

    /**
     * Starts playback of a random in‑game music track. The clip returned
     * will loop continuously until {@link Clip#stop()} is called. Each call
     * selects a different track uniformly at random from the available list.
     *
     * @return a looping music clip, or {@code null} if loading fails
     */
    public static Clip ingame() {
        String randomTrack = INGAME_TRACKS[RANDOM.nextInt(INGAME_TRACKS.length)];
        return play(randomTrack, true);
    }

    /**
     * Plays the introduction music. This clip does not loop; it plays once
     * and then stops.
     *
     * @return a clip playing the intro music, or {@code null} on failure
     */
    public static Clip intro() {
        return play("asset/intro.wav", false);
    }

    /**
     * Begins looping the lobby theme. The clip returned will play
     * continuously until stopped.
     *
     * @return a looping lobby music clip, or {@code null} on failure
     */
    public static Clip lobby() {
        return play("asset/lobby.wav", true);
    }

    /**
     * Plays the positive feedback sound. This clip plays once and does not
     * loop.
     *
     * @return a clip playing the “ok” sound effect, or {@code null} on failure
     */
    public static Clip ok() {
        return play("asset/ok.wav", false);
    }

    /**
     * Starts the results screen music. The clip loops continuously.
     *
     * @return a looping results music clip, or {@code null} on failure
     */
    public static Clip results() {
        return play("asset/results.wav", true);
    }

    /**
     * Plays a short silence or placeholder sound. Used when no valid words
     * are entered. The clip does not loop.
     *
     * @return a clip playing a minimal sound effect, or {@code null} on failure
     */
    public static Clip nothing() {
        return play("asset/nothing.wav", false);
    }

    /**
     * Loads an audio clip from the classpath and starts playback. If the
     * resource cannot be found or an error occurs while opening the clip
     * this method logs the error and returns {@code null}. When the clip
     * finishes playing it is automatically closed along with its associated
     * input streams. If {@code loop} is {@code true} the clip will loop
     * indefinitely; otherwise playback starts immediately and ends once.
     *
     * @param path relative path within the classpath to the WAV resource
     * @param loop whether the clip should loop continuously
     * @return the opened {@link Clip}, or {@code null} if loading fails
     */
    private static Clip play(String path, boolean loop) {
        try {
            InputStream rawStream = GameSound.class.getResourceAsStream(path);

            if (rawStream == null) {
                System.err.println("Sound file not found: " + path);
                return null;
            }

            BufferedInputStream bufferedStream = new BufferedInputStream(rawStream);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedStream);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && !clip.isRunning()) {
                    clip.close();

                    try {
                        audioStream.close();
                        bufferedStream.close();
                    } catch (Exception ignored) {
                    }
                }
            });

            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }

            return clip;

        } catch (Exception e) {
            System.err.println("Failed to play sound: " + path);
            e.printStackTrace();
            return null;
        }
    }
}
