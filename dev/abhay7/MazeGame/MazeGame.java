package dev.abhay7.MazeGame; // My Java Package Name

// Import AWT, TimeUnit, and Swing Libraries
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferStrategy;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities; 

// Declare our MazeGame, which extends Canvas so we can draw graphics, and implements the Runnable interface so we can call it via a Thread
public class MazeGame extends Canvas implements Runnable {

    // Declare Window constants
    public static final String TITLE = "Abhay's Amazing Maze", ICON_PATH = "res\\icon.png";
    public static final int WIDTH = 1280, HEIGHT = WIDTH / 16 * 9; // 16/9 Aspect Ratio
    
    // Declare game loop constants
    public static final int MAX_UPDATES_PER_SECOND = 60;
    public static final int MAX_FRAMES_PER_SECOND = 60;
    public static final double tickLimiter = 300.0;

    // Player constants for starting position
    public static final double playerStartX = 1.5, playerStartY = 1.5, playerStartDirection = 11 * Math.PI / 6, playerFOV = Math.PI / 2, playerVelocityMultiplier = tickLimiter / 20000, playerRotationSpeed = tickLimiter / 30000;
    public static final int tileSideLength = 5; // Sidelength for each tile in the minimap

    // Thread to hold game process, boolean to designate if game is running so we know when to stop
    private Thread gameThread;
    private boolean isRunning = false;
    
    // Actual Player/Camera object
    private Player player;

    // Map used in game. 1 designates a wall; 0 designates a walkable space.
    // Temporary map; the constructor actually initializes it
    public int[][] map = new int[][] {
        {1, 1, 1, 1},
        {1, 0, 0, 1},
        {1, 0, 0, 1},
        {1, 1, 1, 1}
    };

    // Constructor for this mazegame, which essentially initializes a Window while passing "this" game instance as the canvas
    public MazeGame() {
        this.map = MapGenerator.getMap(0);
        new Window(WIDTH, HEIGHT, TITLE, ICON_PATH, this);
    }

    // Starts the game and the game loop after initializing the player. "Synchronizes" means that only one thread can access the method, which ensures that the method isn't run twice accidently
    public synchronized void start() {
        player = new Player(playerStartX, playerStartY, playerStartDirection, playerFOV, playerVelocityMultiplier, playerRotationSpeed); // Initialize player
        this.addKeyListener(player); // Because the player implements "KeyListener," it IS-A key listener which we can attach to the canvas
        gameThread = new Thread(this); // Initialize our game thread and start the program
        gameThread.start();
        isRunning = true;
    }

    // Method to stop the game when we are done running. Cleanly stops program and extra Thread
    public synchronized void stop() {
        try {
            gameThread.join(); // "join()" means stop the thread
            isRunning = false; // Set isRunning to false
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
                tick();
                delta--; // Subtract from delta so we eventually reach and end point
            }
            // isRunning may have changed since we started the loop because this program is multithreaded, so we check once more before rendering the program
            if(isRunning) {
                render(lastFps);
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
    private void tick() {
        player.tick(map); // The only thing we need to tick is the player, and it's tick method requires the map.
    }

    // The "render" method which draws graphics to the screen
    private void render(int lastFps) {

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

        // The next part of the view is our minimap, which we simply render by drawing our 2d array of the map
        for(int i = 0; i < this.map.length; i++) {
            for(int j = 0; j < this.map[0].length; j++) {
                // Draw the wall/floor different colors
                if(this.map[i][j] == 1) {
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

        // We then want to render the player on the minimap itself.
        player.renderMiniMap(g, tileSideLength);
        // We want to draw the crosshair in the center
        player.renderCrosshair(g);

        // Finally, draw our fps
        g.setColor(Color.CYAN);
        g.drawString("FPS: " + lastFps, MazeGame.WIDTH - MazeGame.WIDTH / 20, MazeGame.HEIGHT / 30);

        // Dispose our graphics to release it's resources
        g.dispose();
        bs.show(); // Actually display the graphics to the screen from the bufferstrategy
    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(() -> { // Because JFrame and AWT are NOT thread-safe, we wan't to ensure that updates to the GUI can occur after we update data 
            new MazeGame(); // Create the window and start the game
        });
    }

}
