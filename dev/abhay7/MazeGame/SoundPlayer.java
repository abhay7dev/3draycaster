package dev.abhay7.MazeGame;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundPlayer implements Runnable {

    private AudioInputStream audioStream;
    private Clip audioClip;
    
    public SoundPlayer(String audPath) {
        try {
            File f = new File(audPath);
            this.audioStream = AudioSystem.getAudioInputStream(f);
            audioClip = AudioSystem.getClip();
            audioClip.open(this.audioStream);
        } catch(Exception e) {
            System.out.println("Failed to load " + audPath);
            e.printStackTrace();
        }
    }

    public void run() {
        
    }

    public void start() {
        if(audioClip != null && !audioClip.isActive()) {
            audioClip.start();
            audioClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if(audioClip != null && audioClip.isActive()) {
            audioClip.stop();
        }
    }

    public void destroy() {
        if(audioClip != null) audioClip.close();
        try {
            if(audioStream != null) audioStream.close();
        } catch(Exception e) {
            System.out.println("Failed to close audio stream");
        }
    }

}
