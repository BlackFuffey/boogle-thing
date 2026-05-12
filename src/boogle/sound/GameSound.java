package boogle.sound;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Random;

public final class GameSound {

    private static final Random RANDOM = new Random();

    private static final String[] INGAME_TRACKS = {
            "asset/ingame1.wav",
            "asset/ingame2.wav",
            "asset/ingame3.wav",
            "asset/ingame4.wav"
    };

    private static boolean muted = false;

    public static Clip bad() {
        // 1 in 50 chance to FAHHH
        if (Math.random() > 0.98) {
            return play("asset/bad-alt.wav", false);
        }

        return play("asset/bad.wav", false);
    }

    public static Clip ingame() {
        String randomTrack = INGAME_TRACKS[RANDOM.nextInt(INGAME_TRACKS.length)];
        return play(randomTrack, true);
    }

    public static Clip intro() {
        return play("asset/intro.wav", false);
    }

    public static Clip lobby() {
        return play("asset/lobby.wav", true);
    }

    public static Clip ok() {
        return play("asset/ok.wav", false);
    }

    public static Clip results() {
        return play("asset/results.wav", false);
    }

    public static void setMute(boolean muted) {
        GameSound.muted = muted;
    }

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
