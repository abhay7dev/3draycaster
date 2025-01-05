package dev.abhay7.MazeGame;

// Import AWT & Swing Libraries
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

// Declare Window Class
public class Window extends JFrame {

    private MazeGame game;
    
    // Simple constructor which accepts settings and initializes a window
    public Window(int width, int height, String title, String iconPath, MazeGame game) {
        
        this.setTitle(title); // Set Window Title
        this.setIconImage(new ImageIcon(iconPath).getImage()); // Set Window Icon
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Make close button "Exit" the window
        this.setSize(width, height); // Set size of window
        this.setResizable(false); // Make window NOT resizable
        this.setLocationRelativeTo(null); // Make window centered
        
        this.getContentPane().setBackground(Color.BLACK); // Set the background color by default to be black
        
        this.game = game;
        this.add(game); // Add our game to this JFrame
        this.setVisible(true); // Set this JFrame to be visible

        game.requestFocusInWindow(); // Direct focus of our cursor to the game automatically
        game.start(); // Start the game (Executing the thread)
    }

    @Override
    public void dispose() {
        if(game.player != null) game.player.closeAudioStreams();
        super.dispose();
    }

}
