package boogle.sound;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Random;

/**
 * Static sound-effect and music loader for Boogle.
 *
 * <p>Each method loads a WAV resource from the classpath and starts a
 * {@link Clip}. Missing audio assets are reported to standard error and return
 * {@code null}; the project archive used for documentation review may omit the
 * asset files.</p>
 */
public final class GameSound {

    /**
     * Prevents construction of the static sound utility class.
     */
    private GameSound() {
    }

    private static final Random RANDOM = new Random();

    private static final String[] INGAME_TRACKS = {
            "asset/ingame1.wav",
            "asset/ingame2.wav",
            "asset/ingame3.wav",
            "asset/ingame4.wav"
    };

    private static boolean muted = false;

    /**
     * Plays the invalid-move sound effect, with a rare alternate sound.
     *
     * @return started clip, or {@code null} when the resource cannot be loaded
     */
    public static Clip bad() {
        // 1 in 50 chance to FAHHH
        if (Math.random() > 0.98) {
            return play("asset/bad-alt.wav", false);
        }

        return play("asset/bad.wav", false);
    }

    /**
     * Plays a randomly selected looping in-game music track.
     *
     * @return looping clip, or {@code null} when the resource cannot be loaded
     */
    public static Clip ingame() {
        String randomTrack = INGAME_TRACKS[RANDOM.nextInt(INGAME_TRACKS.length)];
        return play(randomTrack, true);
    }

    /**
     * Plays the title-screen intro sound.
     *
     * @return started clip, or {@code null} when the resource cannot be loaded
     */
    public static Clip intro() {
        return play("asset/intro.wav", false);
    }

    /**
     * Plays the looping lobby music track.
     *
     * @return looping clip, or {@code null} when the resource cannot be loaded
     */
    public static Clip lobby() {
        return play("asset/lobby.wav", true);
    }

    /**
     * Plays the success sound effect.
     *
     * @return started clip, or {@code null} when the resource cannot be loaded
     */
    public static Clip ok() {
        return play("asset/ok.wav", false);
    }

    /**
     * Plays the looping results-screen music track.
     *
     * @return looping clip, or {@code null} when the resource cannot be loaded
     */
    public static Clip results() {
        return play("asset/results.wav", true);
    }

    /**
     * Plays a short silent placeholder sound used when music is disabled.
     *
     * @return started clip, or {@code null} when the resource cannot be loaded
     */
    public static Clip nothing() {
        return play("asset/nothing.wav", false);
    }

    /**
     * Loads and starts an audio clip from a classpath resource.
     *
     * @param path resource path relative to this class's package
     * @param loop whether to loop the clip continuously instead of starting it
     *        once
     * @return opened clip, or {@code null} if loading or playback setup fails
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
