package dev.abhay7.MazeGame;

// Import libraries
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

// SoundPlayer class which plays sounds in a seperate thread
public class SoundPlayer implements Runnable {

    // Hold's an audioinputsteam and clip object
    private AudioInputStream audioStream;
    private Clip audioClip;
    
    public SoundPlayer(String audPath) {
        try {
            // Like we did in the MazeGame class, we use getResource in order to access the sound file
            URL audioUrl = getClass().getResource(audPath);
            if (audioUrl == null) {
                throw new IllegalArgumentException("Audio file not found: " + audPath);
            }

            // We get the input steam, and then get a clip, and then have the clip connect to the inputstream to read the data
            this.audioStream = AudioSystem.getAudioInputStream(audioUrl);
            audioClip = AudioSystem.getClip();
            audioClip.open(this.audioStream);
        } catch(Exception e) {
            System.out.println("Failed to load " + audPath);
            e.printStackTrace();
        }
    }

    // For the thread, we don't need anything to happen right away
    @Override
    public void run() {     
    }

    // When start is called, we want to check if the audio clip is not already playing, then start it
    public void start() {
        if(audioClip != null && !audioClip.isActive()) {
            audioClip.start();
            audioClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // When stop is called, we check if it's active and stop it
    public void stop() {
        if(audioClip != null && audioClip.isActive()) {
            audioClip.stop();
        }
    }

    // Destroy the audio clip to terminate any lingering processes and clean up connections
    public void destroy() {
        if(audioClip != null) audioClip.close();
        try {
            if(audioStream != null) audioStream.close();
        } catch(Exception e) {
            System.out.println("Failed to close audio stream");
        }
    }

}
