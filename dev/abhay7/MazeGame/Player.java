// TODO: Fix fisheye by adjusting angles in for loop instead of using cosine.
package dev.abhay7.MazeGame; // My Java Package Name

// Import AWT Libraries
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Declare Player class, which IS-A key listener which can listen for WASD and respond accordingly
public class Player implements KeyListener {

    // Declare variables for current position, direction faced (radians), fov (radians), and the multipliers for velocity and rotation per second
    private double x, y, direction, fov, velocityMultiplier, rotationSpeed;
    // Declare booleans to hold if movement should be happening.
    private boolean leftIsPressed, rightIsPressed, forwardIsPressed, backIsPressed;
    // Color bounds, declaring the min and max color of the walls
    private int lowerRGBBound = 20, higherRGBBound = 215;

    // Constructor for the Player. Simply sets all these variables
    public Player(double startX, double startY, double direction, double fov, double velocityMultiplier, double rotationSpeed) {
        this.x = startX;
        this.y = startY;
        this.direction = direction;
        this.fov = fov;
        this.velocityMultiplier = velocityMultiplier;
        this.rotationSpeed = rotationSpeed;
    }

    // Getters for X/Y Position
    public double getX() {
        return this.x;
    }
    public double getY() {
        return this.y;
    }

    // Part of KayListener interface.
    @Override
    public void keyPressed(KeyEvent key) {
        // Based on the key that is pressed, we set the corresponding boolean to true
        if((key.getKeyCode() == KeyEvent.VK_UP)) forwardIsPressed = true;
        if((key.getKeyCode() == KeyEvent.VK_LEFT)) leftIsPressed = true;
        if((key.getKeyCode() == KeyEvent.VK_DOWN)) backIsPressed = true;
        if((key.getKeyCode() == KeyEvent.VK_RIGHT)) rightIsPressed = true;
    }

    // Part of KeyListener interface
    @Override
    public void keyReleased(KeyEvent key) {
        // When a key is let go or "released," we make sure that its corresponding boolean is set to false so we stop moving
        if((key.getKeyCode() == KeyEvent.VK_UP)) forwardIsPressed = false;
        if((key.getKeyCode() == KeyEvent.VK_LEFT)) leftIsPressed = false;
        if((key.getKeyCode() == KeyEvent.VK_DOWN)) backIsPressed = false;
        if((key.getKeyCode() == KeyEvent.VK_RIGHT)) rightIsPressed = false;
    }

    // Render the 3D View the player sees, requires graphics and the map to draw.
    public void renderView(Graphics g, int[][] map) {
        // Variable to hold how far off each x value should be for our drawings
        int rectOffset = 0;
        int lineWidth = 4; // Define how many pixels wide each induvidual segment should be.
        int rayCount = MazeGame.WIDTH / lineWidth; // Number of rays to draw

        // For loop which goes through ev ery "Ray" in our Raycasting algorithm. 
        // Remember that direction and fov is in radians, so we look at our direction, then go from left to right for each ray, from half our fov to the left of our direction to half our fov to the right
        for(double dir = (this.direction + (this.fov / 2)); dir >= (this.direction - (this.fov / 2)); dir -= (this.fov / rayCount)) {
        
            // In the near-impossible case our direction is a perfect quarter of the unit circle, I add a marginal value to the ray so that our trigonometric operations always result in a value. 
            if(dir == 0) dir += 0.000001;
            else if(dir == (Math.PI / 2)) dir += 0.000001;
            else if(dir == Math.PI) dir += 0.000001;
            else if(dir == (3 * (Math.PI / 2))) dir += 0.000001;

            /*
             * The following code is where the 3d rendering happens.
             * It was a lot more convoluted and messy at first, but I instead made it messy but also more concise
             * Because I abused ternary operators, my code is hard to follow, but it essentially does trigonometry based on what quadrant the "dir" ray is currently in
            */

            // Messy ternary operator to determine which quadrant the ray is in.
            int quadrant = ((dir > 0 && dir < (Math.PI / 2)) || dir > (2 * Math.PI)) ? 1 : (
                (dir > (Math.PI / 2) && dir < Math.PI) ? 2 : (
                    (dir > Math.PI && dir < (3 * (Math.PI / 2))) ? 3 : (
                        ((dir > (3 * (Math.PI / 2)) && dir < (Math.PI * 2)) || dir < 0) ? 4 : 0
                    )
                )
            );

            // Boolean expressions to make it easier to understand where the ray is, and how we need to apply our trigonometric functions, additions/subtractions, and negations
            boolean top = (quadrant == 1 || quadrant == 2); // Whether ray is pointing upwards (-y in terms of screen, but positive y on unit circle)
            boolean right = (quadrant == 1 || quadrant == 4); // Whether ray is pointing to the right(+x in both screen and unit circle)
            boolean posTan = (quadrant == 1 || quadrant == 3); // Whether ray is poiting to quadrant where tangent ends up being positive (so quad 1 where it is +/+, or quad 3 where it is -/-)

            // The magical steps where distance to nearest walls are calculated
            double horizIntersectionDistance = getHorizIntersectionDistance(dir, top, right, posTan, map);
            double vertIntersectionDistance = getVertIntersectionDistance(dir, top, right, posTan, map);

            // We compare these two values and get the shorter distance, because we want to display the closest wall
            double shorterIntersectionDistance = horizIntersectionDistance < vertIntersectionDistance ? horizIntersectionDistance : vertIntersectionDistance;

            // Draw the wall to the screen by essentially drawing the entire pixel column
            drawWall(g, shorterIntersectionDistance, this.direction, dir, lineWidth, rectOffset, 30, getColorFromDistance(shorterIntersectionDistance, Color.WHITE, dir, 2), getColorFromDistance(shorterIntersectionDistance, Color.DARK_GRAY, dir, 0.4));

            // Update our offset for the next column
            rectOffset += lineWidth;
        }

    }

    // Magical math part 1. For a ray, this method finds the distance to the nearest horizontal wall that the ray would hit
    private double getHorizIntersectionDistance(double dir, boolean top, boolean right, boolean posTan, int[][] map) {
        // These two lines find the distance to the nearest integer array index in the y axis, and then it finds the x axis distance needed to travel with the current angle in order to reach this flat y
        double nearestYOffset = top ? (this.y - ((int) (this.y))) : (((int) (this.y + 1)) - this.y);
        double nearestXOffset = posTan ? (nearestYOffset / Math.tan(dir)) : (nearestYOffset / (-Math.tan(dir)));

        // Variable which holds the total distance travelled so far in the x and y directions
        double totalXOffsetHorizInter = nearestXOffset;
        double totalYOffsetHorizInter = nearestYOffset;

        // Now, we travel vertically in units of 1, and our horizontal distance travelled every time is the x distance required to move 1 unit at our current ray angle
        double normalYOffset = 1;
        double normalXOffset = posTan ? (normalYOffset / Math.tan(dir)) : (normalYOffset / (-Math.tan(dir)));

        // The following loop essentially adds to the totall x and y distance variables for the nearest wall.
        // The first 4 conditions ensures our ray stays within the bounds of the array
        // The last condition allows the loop to keep going until it hits a wall.
        // We have a bunch of ternary operators because the values we use will depend on which quadrant our ray is in, because that changes the values our trigonometric functions return
        while(
            (((int) (right ? (x + totalXOffsetHorizInter) : (x - totalXOffsetHorizInter))) > -1) &&
            (((int) (right ? (x + totalXOffsetHorizInter) : (x - totalXOffsetHorizInter))) < map[0].length) &&
            (((int) (top   ? (y - totalYOffsetHorizInter - normalYOffset) : (y + totalYOffsetHorizInter))) > -1) &&
            (((int) (top   ? (y - totalYOffsetHorizInter - normalYOffset) : (y + totalYOffsetHorizInter))) < map.length) &&
            (map[
                ((int) (top ? (y - totalYOffsetHorizInter - normalYOffset) : (y + totalYOffsetHorizInter)))
            ][
                ((int) (right ? (x + totalXOffsetHorizInter) : (x - totalXOffsetHorizInter)))
            ] < 1)
        ) {
            totalXOffsetHorizInter += normalXOffset;
            totalYOffsetHorizInter += normalYOffset;
        }

        // Use trig to get the distance
        return top ? (totalYOffsetHorizInter / Math.sin(dir)) : -(totalYOffsetHorizInter / Math.sin(dir));
    }

    // Magical math part 2
    // Follows the same method as the above but adjusted to find the enarest vertical wall intersection
    private double getVertIntersectionDistance(double dir, boolean top, boolean right, boolean posTan, int[][] map) {
        double nearestXOffset = right ? (((int) (this.x + 1)) - this.x) : (this.x - ((int) (this.x)));
        double nearestYOffset = posTan ? (Math.tan(dir)) * nearestXOffset : (-Math.tan(dir)) * nearestXOffset;

        double totalXOffsetVertInter = nearestXOffset;
        double totalYOffsetVertInter = nearestYOffset;

        double normalXOffset = 1;
        double normalYOffset = posTan ? (Math.tan(dir)) * normalXOffset : (-Math.tan(dir)) * normalXOffset;

        while(
            (((int) (right ? (x + totalXOffsetVertInter) : (x - totalXOffsetVertInter - normalXOffset))) > -1) &&
            (((int) (right ? (x + totalXOffsetVertInter) : (x - totalXOffsetVertInter - normalXOffset))) < map[0].length) &&
            (((int) (top   ? (y - totalYOffsetVertInter) : (y + totalYOffsetVertInter))) > -1) &&
            (((int) (top   ? (y - totalYOffsetVertInter) : (y + totalYOffsetVertInter))) < map.length) &&
            map[
                ((int) (top   ? (y - totalYOffsetVertInter) : (y + totalYOffsetVertInter)))
            ][
                ((int) (right ? (x + totalXOffsetVertInter) : (x - totalXOffsetVertInter - normalXOffset)))
            ] < 1
        ) {
            totalXOffsetVertInter += normalXOffset;
            totalYOffsetVertInter += normalYOffset;
        }

        return top ? (totalYOffsetVertInter / Math.sin(dir)) : -(totalYOffsetVertInter / Math.sin(dir));
    }

    // Method to determine the color something should be based on distance. The closer something is, the brighter it will be. This simulates a flashlight
    private Color getColorFromDistance(double distance, Color baseColor, double dir, double lightLimiter) {
        double flashLightMultiplier = (Math.abs(dir - this.direction)) * lightLimiter / (0.1 * distance); // The larger this is, the less spread our light will have
        double distanceMultiplier = distance / 10; // The closer it is, the less we want to decrease from 245
        // Our palette is based on the baseColor, so it can adapt to any wall color.
        baseColor = getColorWithinBounds(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (-(baseColor.getRed() * flashLightMultiplier * distanceMultiplier)));
        return baseColor;
    }

    // Method to draw the actual wall the player sees
    private void drawWall(Graphics g, double rawIntersectionDistance, double mainDirection, double rayDir, int lineWidth, int rectOffset, int circularLightRange, Color wallColor, Color floorColor) {
        double diffAngle = mainDirection - rayDir; // Calculate the difference the ray angle has compared to the direction angle, which...
        rawIntersectionDistance *= Math.cos(diffAngle); // we then use to calculate simply the horizontal distance.
        // remember that cos is adj/hyp, and rawIntersectionDistance would be a hypotenuse.
        // By getting simply this adjacent length, we are minimizing the "fish eye" effect that we have because our for loop in renderView() goes at equaly spaced angles, which makes our camera have a larger distance in the edges of the screen

        int rectHeight = (int) ((MazeGame.HEIGHT) / (rawIntersectionDistance + 1)); // We get the height of the rectangle we want to draw. We add by 1 so that the rectangle isn't super big on the screen, but the bigger that number gets, the smaller the rectangles
    
        // Draw our night sky
        g.setColor(new Color(7, 11, 52));
        g.fillRect(rectOffset, 0, lineWidth, (MazeGame.HEIGHT - (rectHeight))/2);
        // Draw stars in our night sky.
        if((Math.random()) > .85) {// Draw star only 15% of the time
            g.setColor(Color.CYAN);
            // Randomly choose an x/y position inside the current rectangle, then draw a rounded rectangle for the "star"
            g.fillRoundRect(rectOffset + ((int) (Math.random() * 4)), (int) (Math.random() * ((MazeGame.HEIGHT - (rectHeight))/2)), 2, 2, 1, 1);
        }

        // Loop to draw the wall in many horizontal subdivisions with different colors to represent lighting in a circular fashion
        for(int i = -(circularLightRange / 2); i < (circularLightRange / 2); i++) {
            g.setColor(getColorWithinBounds(wallColor.getRed(), wallColor.getGreen(), wallColor.getBlue(), (-Math.abs(i))));
            g.fillRect(rectOffset, (MazeGame.HEIGHT - (rectHeight))/2 + (rectHeight / 2 + (i * rectHeight/circularLightRange)), lineWidth, rectHeight/circularLightRange + 2);
        }

        // Draw the floor
        g.setColor(floorColor);
        g.fillRect(rectOffset, rectHeight + (MazeGame.HEIGHT - (rectHeight))/2, lineWidth, (MazeGame.HEIGHT - (rectHeight))/2);
    }

    // Helper method to ensure wall colors are within bounds
    private Color getColorWithinBounds(double r, double g, double b, double offset) {
        return new Color((int) Math.max(lowerRGBBound, Math.min(higherRGBBound, r + offset)), (int) Math.max(lowerRGBBound, Math.min(higherRGBBound, g + offset)), (int) Math.max(lowerRGBBound, Math.min(higherRGBBound, b + offset)));
    }

    // Render the player square on the minimap. 
    public void renderMiniMap(Graphics g, int tileSideLength) {
        int playerSize = tileSideLength / 5; // Determine size of player on map
        // Determine the top left of the player, because we draw squares from the top left.
        int xTopLeft = (int) ((this.x * tileSideLength) - playerSize / 2);
        int yTopLeft = (int) ((this.y * tileSideLength) - playerSize / 2);
        // Draw the player in as a white square
        g.setColor(Color.WHITE);
        g.fillRect(xTopLeft, yTopLeft, playerSize, playerSize);
    }

    // Render the crosshair. 
    public void renderCrosshair(Graphics g) {
        int midX = MazeGame.WIDTH / 2;
        int midY = MazeGame.HEIGHT / 2;

        int crossThickness = 2;
        int crossHeight = 8;

        g.setColor(new Color(7, 11, 52));
        g.fillRect(midX - crossThickness / 2, midY - crossHeight / 2, crossThickness, crossHeight);
        g.fillRect(midX - crossHeight / 2, midY - crossThickness / 2, crossHeight, crossThickness);
    }

    // Player "tick()" method, which updates data (x/y position mainly).
    public void tick(int[][] map) {
        
        // If both of them are pressed, then don't move
        if(!(this.leftIsPressed && this.rightIsPressed)) {
            if(this.leftIsPressed) { // If the left button is pressed, then move rotate our camera to the left
                direction += rotationSpeed; // Increases our direction by "rotation speed" amount. A positive value turns the character left
            } else if(this.rightIsPressed) {
                direction -= rotationSpeed; // Decreases our direction, turning right
            }
            // If our direction is less than 0 or greater than 2PI, then we want to reset it to stay within our bounds of [2, 2PI]
            if(direction >= (2 * Math.PI)) direction -= (2 * Math.PI);
            if(direction < 0) direction += (2 * Math.PI);
        }

        // Like previously, we check to make sure both forward & back aren't pressed 
        if(!(this.forwardIsPressed && this.backIsPressed)) {
            // Use Trig Identities to see how much x and y we need to add
            double xOff = (Math.cos(this.direction) * velocityMultiplier);
            double yOff = -(Math.sin(this.direction) * velocityMultiplier);    

            if(this.forwardIsPressed) {
                // If the space in front of us in the horizontal direction is open, then we increment x
                if(map[(int)this.y][(int) (this.x + xOff)] == 0){
                    this.x += xOff;
                }
                // If the space in front of us in the vertical direction is open, then we increment y
                if(map[(int)(this.y + yOff)][(int) (this.x)] == 0){
                    this.y += yOff;
                }
            } else if(this.backIsPressed) {
                // As before, we do the same checks then change our x/y value by the calculated offset.
                // The difference, however, is that because we are moving backwards, we need to subtract the values
                if(map[(int)this.y][(int) (this.x - xOff)] == 0){
                    this.x -= xOff;
                }
                if(map[(int)(this.y - yOff)][(int) (this.x)] == 0){
                    this.y -= yOff;
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {} // Do nothing; We don't really care about this event
}
