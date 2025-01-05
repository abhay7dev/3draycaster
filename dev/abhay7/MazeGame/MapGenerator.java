package dev.abhay7.MazeGame;

// Import packages to read Campaign Maps & Generate Mazes
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class MapGenerator {

    // Declare 3d map array for campaign
    // Essentially, it is an array of 2d "maps"
    public Integer[][][] maps;

    // Constructor to read campaign maps
    public MapGenerator() {
        // Following the system in MazeGame.java, we use an input stream in order to get a steam a scanner can read from.
        try (InputStream inputStream = getClass().getResourceAsStream(MazeGame.MAPS_PATH)) {
            Scanner scan = new Scanner(inputStream);
            
            // ArrayList to hold the maps, we use Integer instead of int because ArrayLists can't take primitives
            ArrayList<Integer[][]> mapsList = new ArrayList<>();

            // ArrayList to hold number of rows in each map
            ArrayList<Integer[]> rows = new ArrayList<>();
            int lastLength = -1; // hold length of first line in map. Invalidly created maps will throw errors and cause issues
            while(scan.hasNextLine()) {
                String next = scan.nextLine(); // Read line
                
                // If the line is ENDL, that means we read a map, and we can move on to the next one
                if(next.equals("ENDL")) {
                    lastLength = -1;
                    // Copy our arraylist data to an Integer[][] 
                    Integer[][] mapToAdd = new Integer[rows.size()][rows.get(0).length];
                    for(int i = 0; i < rows.size(); i++) {
                        for(int j = 0; j < rows.get(i).length; j++) {
                            mapToAdd[i][j] = rows.get(i)[j];
                        }
                    }
                    // Add the map and reset our rows
                    mapsList.add(mapToAdd);
                    rows = new ArrayList<>();
                } else {
                    // Read the line and add it's tokens (2, 1, 0) to the arraylist
                    String[] tokens = next.split("");
                    Integer[] row = new Integer[tokens.length];
                    if(lastLength < 0) lastLength = tokens.length;
                    for(int i = 0; i < lastLength; i++) {
                        row[i] = Integer.parseInt(tokens[i]);
                    }
                    rows.add(row);
                }
            }

            // Initialize the maps 3d array
            maps = new Integer[mapsList.size()][][];
            for(int i = 0; i < mapsList.size(); i++) {
                maps[i] = mapsList.get(i);
            }

            // Close the scanner
            scan.close();
        } catch(Exception e) {
            System.err.println("Failed to read res/maps.txt! Levels will not be read.\n");
            e.printStackTrace();

            // Create a default map 3d array
            maps = new Integer[][][] {
                {
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1},
                    {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                }
            };
        }
    }

    // Return a specific level
    public Integer[][] getMap(int level) {
        return maps[level];
    }

    // Maze Generation (Following a DFS algorithm). Makes a square maze with side lengths of a minimum size of 5
    public Integer[][] generateMaze(int size) {
        if(size < 5) throw new IllegalArgumentException("Argument " + size + " is less than 5");
        Integer[][] maze = new Integer[size][size];

        // By default, everything is a wall
        for(int i = 0; i < maze.length; i++) {
            for(int j = 0; j < maze[i].length; j++) {
                maze[i][j] = 1;
            }
        }

        // Create a Coord for the starting point, and make that spot empty
        Coord start = new Coord(1, 1);
        maze[start.y][start.x] = 0;

        // Make a Stack for the DFS algorithm. Holds the visited coords that need to be rechecked
        Stack<Coord> coordStack = new Stack<Coord>();
        coordStack.add(start);
        
        // Call DFS Generation, updating our maze 2d array
        dfsGeneration(start, coordStack, maze);

        // Find a coordinate where I can put a finish line
        Coord goalCoord = new Coord(maze.length - 1, maze[maze.length - 1].length - 1);

        // Start from the bottom right, and go up little by little to find a suitable spot for the end coordinate
        while(maze[goalCoord.y][goalCoord.x] == 1 && goalCoord.y > -1 && goalCoord.x > -1) {
            if(maze[goalCoord.y - 1][goalCoord.x] == 0) goalCoord = new Coord(goalCoord.y - 1, goalCoord.x);
            else if(maze[goalCoord.y][goalCoord.x - 1] == 0) goalCoord = new Coord(goalCoord.y, goalCoord.x - 1);
            else goalCoord = new Coord(goalCoord.y - 1, goalCoord.x - 1);
        }
        maze[goalCoord.y][goalCoord.x] = 2;

        return maze;
    }

    // Recursive method to generate the maze
    private void dfsGeneration(Coord curr, Stack<Coord> coords, Integer[][] maze) {
        if(coords.size() == 0) return; // If the stack is exhausted, that means the generation is complete
        ArrayList<Coord> availCoords = getAvailCoords(curr, maze); // Get the coordinates that we can move to
        if(availCoords.size() > 0) { // If there are available coordinate, randomly go to one of them, make it a 0 to designate a path, then move to that tile and check once more
            Coord randCoord = availCoords.get((int)(Math.random() * availCoords.size()));
            maze[randCoord.y][randCoord.x] = 0;
            coords.push(randCoord);
            dfsGeneration(randCoord, coords, maze);
        } else { // If there are no available spots, we need to recurse back down the stack
            Coord last = coords.pop();
            dfsGeneration(last, coords, maze);
        }
    }

    // Method to get the available coordinates the dfs can move to
    private ArrayList<Coord> getAvailCoords(Coord curr, Integer[][] maze) {
        
        // ArrayList to hold possible coordinates
        ArrayList<Coord> coords = new ArrayList<Coord>();

        // Check above the current coordinate, and then to the left & right if there can be a wall there (there has to be walls on 3 sides around it)
        int yPos = curr.y - 1;
        if(yPos > 0) {
            if(maze[yPos][curr.x] != 0 ) {
                if((yPos > 0 && maze[yPos - 1][curr.x] == 1) && (curr.x > 0 && maze[yPos][curr.x - 1] == 1) && (curr.x < maze[yPos].length && maze[yPos][curr.x + 1] == 1)) {
                    coords.add(new Coord(yPos, curr.x));
                }
            }
        }

        // Same concept as previous, but check below
        yPos = curr.y + 1;
        if(yPos < maze.length) {
            if(maze[yPos][curr.x] != 0 ) {
                if((yPos < maze.length - 1 && maze[yPos + 1][curr.x] == 1) && (curr.x > 0 && maze[yPos][curr.x - 1] == 1) && (curr.x < maze[yPos].length  && maze[yPos][curr.x + 1] == 1)) {
                    coords.add(new Coord(yPos, curr.x));
                }
            }
        }

        // Same concept as previous, but check to the left
        int xPos = curr.x - 1;
        if(xPos > 0) {
            if(maze[curr.y][xPos] != 0 ) {
                if((xPos > 0 && maze[curr.y][xPos - 1] == 1) && (curr.y > 0 && maze[curr.y - 1][xPos] == 1) && (curr.y < maze.length && maze[curr.y + 1][xPos] == 1)) {
                    coords.add(new Coord(curr.y, xPos));
                }
            }
        }
        
        // Same concept as previous, but check to the right
        xPos = curr.x + 1;
        if(xPos < maze[curr.y].length) {
            if(maze[curr.y][xPos] != 0 ) {
                if((xPos < maze[curr.y].length - 1 && maze[curr.y][xPos + 1] == 1) && (curr.y > 0 && maze[curr.y - 1][xPos] == 1) && (curr.y < maze.length && maze[curr.y + 1][xPos] == 1)) {
                    coords.add(new Coord(curr.y, xPos));
                }
            }
        }

        return coords; // Return the possible coordinates
    }

    // Private class to hold an x/y coord
    private class Coord {
        private int y, x;
        Coord(int y, int x) {
            this.y = y;
            this.x = x;
        }
    }

}