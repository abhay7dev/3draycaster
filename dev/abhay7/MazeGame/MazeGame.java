package dev.abhay7.MazeGame; // My Java Package Name

// Import AWT, TimeUnit, and Swing Libraries
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;

import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import java.io.File;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// Declare our MazeGame, which extends Canvas so we can draw graphics, and implements the Runnable interface so we can call it via a Thread
public class MazeGame extends Canvas implements Runnable, MouseListener {

    // Declare Window constants
    public static final String TITLE = "Abhay's Amazing Maze", ICON_PATH = "res\\icon.png";
    public static final int WIDTH = 1280, HEIGHT = WIDTH / 16 * 9; // 16/9 Aspect Ratio
    
    // Declare game loop constants
    public static final int MAX_UPDATES_PER_SECOND = 60;
    public static final int MAX_FRAMES_PER_SECOND = 60;
    public static final double tickLimiter = 300.0;

    // Player constants for starting position
    public static final double playerStartX = 1.5, playerStartY = 1.5, playerStartDirection = 11 * Math.PI / 6, playerFOV = Math.PI / 1.7, playerVelocityMultiplier = tickLimiter / 20000, playerRotationSpeed = tickLimiter / 30000;
    public static final int tileSideLength = 5; // Sidelength for each tile in the minimap

    // Thread to hold game process, boolean to designate if game window is running so we know when to stop
    private Thread gameThread;
    private boolean isRunning = false;
    private boolean isPlaying = false; // Boolean to hold whether we are actually playing the game or are in a menu.

    // Actual Player/Camera object
    public Player player;

    // Resources
    public static final String MAPS_PATH = "res/maps.txt";
    public static final String BACKGROUND_IMAGE_PATH = "res/bgimage.jpg";
    public static final String TITLE_FONT_PATH = "res/valorant.ttf";
    public static final String BUTTON_FONT_PATH = "res/nintendo.otf";
    public static final String WALKING_SFX = "res/footsteps.wav";
    public static final String GAME_SOUND = "res/MazeGameSong.wav";
    private Image backgroundImage;
    private Font titleFont;
    private Font buttonFont;
    private Font defaultFont = new JLabel().getFont();
    private SoundPlayer sp;
    private Thread soundThread;

    // Menu Button Variables
    private int playButX1 = 0, playButY1 = 0, playButX2 = 0, playButY2 = 0;
    private int quitButX1 = 0, quitButY1 = 0, quitButX2 = 0, quitButY2 = 0;
    private int easyButX1 = 0, easyButY1 = 0, easyButX2 = 0, easyButY2 = 0;
    private int hardButX1 = 0, hardButY1 = 0, hardButX2 = 0, hardButY2 = 0;
    private int genButX1 = 0, genButY1 = 0, genButX2 = 0, genButY2 = 0;
    private boolean isEasyMode = true;
    private boolean isGenMap = false;

    // Maps used in game. 2 designates the end; 1 designates a wall; 0 designates a walkable space.
    // Temporary map in case of error; the constructor actually initializes it.
    public Integer[][] map = new Integer[][] {
        {1, 1, 1, 1, 1},
        {1, 0, 0, 0, 1},
        {1, 0, 0, 0, 1},
        {1, 0, 0, 2, 1},
        {1, 1, 1, 1, 1}
    };

    // Hold the current level the player is attempting and how many levels there are
    private int currentLevel = 0;
    private int levelCount = 1;

    // Constructor for this mazegame, which essentially initializes a Window while passing "this" game instance as the canvas
    public MazeGame() {
        // Initialize our campaign maps
        this.map = MapGenerator.getMap(currentLevel);
        levelCount = MapGenerator.maps.length;

        sp = new SoundPlayer(GAME_SOUND);
        soundThread = new Thread(sp);
        soundThread.start();
        
        // Initialize Resources
        try {
            File bgImgFile = new File(BACKGROUND_IMAGE_PATH);
            backgroundImage = ImageIO.read(bgImgFile);
        } catch(Exception e) {
            backgroundImage = null;
            e.printStackTrace();
        }

        try {
            File fontFile = new File(TITLE_FONT_PATH);
            titleFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            titleFont = titleFont.deriveFont(titleFont.getSize() * (WIDTH / 20) * 1.0f);
        } catch(Exception e) {
            titleFont = null;
        }

        try {
            File fontFile = new File(BUTTON_FONT_PATH);
            buttonFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            buttonFont = buttonFont.deriveFont(buttonFont.getSize() * (WIDTH / 20) * 1.0f);
        } catch(Exception e) {
            buttonFont = null;
        }
        
        // Create the window
        new Window(WIDTH, HEIGHT, TITLE, ICON_PATH, this);
    }

    // Starts the game and the game loop after initializing the player. "Synchronizes" means that only one thread can access the method, which ensures that the method isn't run twice accidently
    public synchronized void start() {
        player = new Player(playerStartX, playerStartY, playerStartDirection, playerFOV, playerVelocityMultiplier, playerRotationSpeed, WALKING_SFX); // Initialize player
        this.addKeyListener(player); // Because the player implements "KeyListener," it IS-A key listener which we can attach to the canvas
        this.addMouseListener(this);
        gameThread = new Thread(this); // Initialize our game thread and start the program
        gameThread.start();
        isRunning = true;
        sp.start();
    }

    // Method to stop the game when we are done running. Cleanly stops program and extra Thread
    public synchronized void stop() {
        try {
            gameThread.join(); // "join()" means stop the thread
            isRunning = false; // Set isRunning to false
            player.closeAudioStreams();
            sp.stop();
            soundThread.join();
        } catch(Exception e) {
            e.printStackTrace(); // If there is an issue, we just print the error chain so we know where it occured 
        }
    }

    // Because our MazeGame class implements runnable, it needs to implement a method that all "Runnable" objects have, which is run(). This is what is called when a thread is ran.
    @Override
    public void run() {

        // The following code is a common, simple game loop which both A) Renders graphics and B) Updates variables ("Ticks").
        long lastTime = System.nanoTime();
        double ns = 1000000000 / tickLimiter; // The higher the tick limiter, the more updates we have per second. (Smaller nanosecond interval between each update AKA "More Ticks")
        
        double delta = 0; // Variable to hold change since last time
        long lastTimer = System.currentTimeMillis(); // Hold ms so we can later calculate FPS
        int frames = 0; // Hold how many frames have passed
        int lastFps = frames;

        while(isRunning) { // While the game is running
            long now = System.nanoTime(); // Get the current time so we can see how much time has elapsed since "lastTime"
            delta += (now - lastTime) / ns; // Add to our delta value, which will tell us how many updates we need
            lastTime = now; // Update lastTime variable so it is ready for the next iteration of the loop
            while(delta >= 1) { // For every delta, we need to "tick()" our variables so our position/collision detection is updated
                if(isPlaying) tickGame();
                delta--; // Subtract from delta so we eventually reach and end point
            }
            // isRunning may have changed since we started the loop because this program is multithreaded, so we check once more before rendering the program
            if(isRunning) {
                if(isPlaying) renderGame(lastFps);
                else renderMenu(lastFps);
            }
            frames++; // This entire update constitues one frame

            if(System.currentTimeMillis() - lastTimer > 1000) { // If 1 second has elapsed, then we should display out the amount of frames we have and then update "lastTimer" to prepare it to display the next FPS.
                lastTimer += 1000;
                lastFps = frames;
                frames = 0;
            }

            try {
                // "Sleep" the loop for a set number of MS to prevent the program from speeding too fast
                TimeUnit.MILLISECONDS.sleep((long) (1000 / tickLimiter));
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
        }

        // if "isRunning" is externally set to false, then we need to call stop to make sure our thread gets destroyed
        stop();
    }
    
    // The "update" method which updates in game variables and values, such as x & y positions
    private void tickGame() {
        player.tick(map); // The only thing we need to tick is the player, and it's tick method requires the map.
        if(player.getNeedsNextLevel() == true) {
            player.setNeedsNextLevel(false);
            currentLevel++;
            if(currentLevel >= levelCount || isGenMap) {
                isPlaying = false;
                currentLevel = 0;
                JOptionPane.showMessageDialog(this, "Congrats, you beat the level(s)!");
            }
            player.setX(playerStartX);
            player.setY(playerStartY);
            player.setDirection(playerStartDirection);
            map = MapGenerator.getMap(currentLevel);
        }
        if(player.getQuitIsPressed() == true) {
            player.setQuitIsPressed(false);
            player.setNeedsNextLevel(false);
            isPlaying = false;
            currentLevel = 0;
            player.setX(playerStartX);
            player.setY(playerStartY);
            player.setDirection(playerStartDirection);
            map = MapGenerator.getMap(currentLevel);
            isGenMap = false;
        }
    }

    // The "render" method which draws graphics to the screen
    private void renderGame(int lastFps) {

        // BufferStrategy is where graphics are essentially the location where graphics are drawn to before we eventually "show" the graphics on the screen
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null) { // The bufferstrategy will be null at the start, so we need to initialize it
            this.createBufferStrategy(3); // This sets up a more resource intensive but more powerful "Triple Buffer" system which constantly utilizes the processor so there always is a frame available for display
            return;
        }

        // We retrieve the graphics object we need from the bufferstrategy, where again, we draw our graphics.
        Graphics g = bs.getDrawGraphics();

        // Fill in the background with a black rectangle
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // The next layer should be the 3d view the player sees
        player.renderView(g, map);

        if(isEasyMode) {
            // The next part of the view is our minimap, which we simply render by drawing our 2d array of the map
            for(int i = 0; i < this.map.length; i++) {
                for(int j = 0; j < this.map[0].length; j++) {
                    // Draw the wall/floor different colors
                    if(this.map[i][j] == 2) {
                        g.setColor(Color.GREEN);
                    } else if(this.map[i][j] == 1) {
                        g.setColor(Color.LIGHT_GRAY);
                    } else {
                        g.setColor(Color.DARK_GRAY);
                    }
                    // Fill in the tile and then draw a black outline
                    g.fillRect(j * tileSideLength, i * tileSideLength, tileSideLength, tileSideLength);
                    g.setColor(Color.BLACK);
                    g.drawRect(j * tileSideLength, i * tileSideLength, tileSideLength, tileSideLength);
                }
            }
        }

        // We then want to render the player on the minimap itself.
        player.renderMiniMap(g, tileSideLength);
        // We want to draw the crosshair in the center
        player.renderCrosshair(g);

        // Finally, draw our fps
        g.setColor(Color.GREEN);
        g.drawString(getFPSString(lastFps), MazeGame.WIDTH - MazeGame.WIDTH / 20, MazeGame.HEIGHT / 30);

        // Dispose our graphics to release it's resources
        g.dispose();
        bs.show(); // Actually display the graphics to the screen from the bufferstrategy
    }

    private void renderMenu(int lastFps) {
        // BufferStrategy is where graphics are essentially the location where graphics are drawn to before we eventually "show" the graphics on the screen
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null) { // The bufferstrategy will be null at the start, so we need to initialize it
            this.createBufferStrategy(3); // This sets up a more resource intensive but more powerful "Triple Buffer" system which constantly utilizes the processor so there always is a frame available for display
            return;
        }

        // We retrieve the graphics object we need from the bufferstrategy, where again, we draw our graphics.
        Graphics g = bs.getDrawGraphics();
        
        // Draw our menu background image
        if(backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this);
        }
        if(titleFont != null) g.setFont(titleFont);

        // Draw our menu title and buttons
        g.setColor(Color.YELLOW);
        FontMetrics fontMetrics = g.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(TITLE);
        g.drawString(TITLE, (WIDTH - textWidth) / 2, HEIGHT / 4);

        g.setFont(buttonFont);
        fontMetrics = g.getFontMetrics();
        String playStr = "Play";
        String quitStr = "Quit";

        textWidth = fontMetrics.stringWidth(playStr);
        g.setColor(Color.CYAN);
        g.fillRect((WIDTH - textWidth) / 4 - 10, (HEIGHT / 2) - (int) (fontMetrics.getHeight() / 1.5), textWidth + 20, (fontMetrics.getHeight() + 20));
        g.setColor(Color.DARK_GRAY);
        g.drawString(playStr, (WIDTH - textWidth) / 4, HEIGHT / 2);
        
        playButX1 = (WIDTH - textWidth) / 4 - 10;
        playButY1 = (HEIGHT / 2) - (int) (fontMetrics.getHeight() / 1.5);
        playButX2 = playButX1 + textWidth + 20;
        playButY2 = playButY1 + (fontMetrics.getHeight() + 20);

        g.setColor(Color.CYAN);
        textWidth = fontMetrics.stringWidth(quitStr);
        g.fillRect((WIDTH - textWidth) / 2 + (WIDTH - textWidth) / 4 - 10, (HEIGHT / 2) - (int) (fontMetrics.getHeight() / 1.5), textWidth + 20, (fontMetrics.getHeight() + 20));
        g.setColor(Color.DARK_GRAY);
        g.drawString(quitStr, (WIDTH - textWidth) / 2 + (WIDTH - textWidth) / 4, HEIGHT / 2);

        quitButX1 = (WIDTH - textWidth) / 2 + (WIDTH - textWidth) / 4 - 10;
        quitButY1 = (HEIGHT / 2) - (int) (fontMetrics.getHeight() / 1.5);
        quitButX2 = quitButX1 + textWidth + 20;
        quitButY2 = quitButY1 + (fontMetrics.getHeight() + 20);

        g.setFont(titleFont);
        fontMetrics = g.getFontMetrics();
        String setDiffString = "Current Difficulty: " + (isEasyMode ? "Easy" : "Hard");
        textWidth = fontMetrics.stringWidth(setDiffString);
        g.setColor(Color.GREEN);
        g.drawString(setDiffString, (WIDTH - textWidth) / 2, (int) (HEIGHT / 1.5));

        String easyStr = "Easy";
        g.setColor(Color.CYAN);
        textWidth = fontMetrics.stringWidth(easyStr);
        g.fillRect((WIDTH - textWidth) / 4 - 10, (int) (HEIGHT / 1.2) - fontMetrics.getHeight(), textWidth + 20, (fontMetrics.getHeight() + 20));
        g.setColor(Color.DARK_GRAY);
        g.drawString(easyStr, (WIDTH - textWidth) / 4, (int) (HEIGHT / 1.2));

        easyButX1 = (WIDTH - textWidth) / 4 - 10;
        easyButY1 = (int) (HEIGHT / 1.2) - fontMetrics.getHeight();
        easyButX2 = easyButX1 + textWidth + 20;
        easyButY2 = easyButY1 + (fontMetrics.getHeight() + 20);
        
        String hardStr = "Hard";
        g.setColor(Color.CYAN);
        textWidth = fontMetrics.stringWidth(hardStr);
        g.fillRect((WIDTH - textWidth) / 2 + (WIDTH - textWidth) / 4 - 10, (int) (HEIGHT / 1.2) - fontMetrics.getHeight(), textWidth + 20, (fontMetrics.getHeight() + 20));
        g.setColor(Color.DARK_GRAY);
        g.drawString(hardStr, (WIDTH - textWidth) / 2 + (WIDTH - textWidth) / 4, (int) (HEIGHT / 1.2));

        hardButX1 = (WIDTH - textWidth) / 2 + (WIDTH - textWidth) / 4 - 10;
        hardButY1 = (int) (HEIGHT / 1.2) - fontMetrics.getHeight();
        hardButX2 = hardButX1 + textWidth + 20;
        hardButY2 = hardButY1 + (fontMetrics.getHeight() + 20);

        String genStr = "Gen Map";
        g.setColor(Color.CYAN);
        textWidth = fontMetrics.stringWidth(genStr);
        g.fillRect(10, 10, textWidth + 20, (fontMetrics.getHeight() + 20));
        g.setColor(Color.DARK_GRAY);
        g.drawString(genStr, 20, 20 + fontMetrics.getHeight());

        genButX1 = 10;
        genButY1 = 10;
        genButX2 = genButX1 + textWidth + 20;
        genButY2 = genButY1 + (fontMetrics.getHeight() + 20);

        // Finally, draw our fps
        g.setFont(defaultFont);
        g.setColor(Color.GREEN);
        g.drawString(getFPSString(lastFps), MazeGame.WIDTH - MazeGame.WIDTH / 19, MazeGame.HEIGHT / 30);

        // Dispose our graphics to release it's resources
        g.dispose();
        bs.show(); // Actually display the graphics to the screen from the bufferstrategy
    }

    public String getFPSString(int fps) {
        if(fps > 99) return "FPS: 99+";
        else return "FPS: " + fps;
    }

    public static void main(String... args) {
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch(Exception e){}
        SwingUtilities.invokeLater(() -> { // Because JFrame and AWT are NOT thread-safe, we wan't to ensure that updates to the GUI can occur after we update data 
            new MazeGame(); // Create the window and start the game
        });
    }

    // Initialize button behaviors
    @Override
    public void mouseClicked(MouseEvent e) {
        if(!isPlaying) {
            if(e.getX() > playButX1 && e.getX() < playButX2 && e.getY() > playButY1 && e.getY() < playButY2) isPlaying = true;
            else if(e.getX() > quitButX1 && e.getX() < quitButX2 && e.getY() > quitButY1 && e.getY() < quitButY2) {
                isRunning = false;
                System.exit(0);
            }
            else if(e.getX() > easyButX1 && e.getX() < easyButX2 && e.getY() > easyButY1 && e.getY() < easyButY2) isEasyMode = true;
            else if(e.getX() > hardButX1 && e.getX() < hardButX2 && e.getY() > hardButY1 && e.getY() < hardButY2) isEasyMode = false;
            else if(e.getX() > genButX1 && e.getX() < genButX2 && e.getY() > genButY1 && e.getY() < genButY2) {
                map = MapGenerator.generateMaze(35);
                isPlaying = true;
                isGenMap = true;
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}

}
