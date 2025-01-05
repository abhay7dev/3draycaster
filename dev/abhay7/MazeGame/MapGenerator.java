package dev.abhay7.MazeGame;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class MapGenerator {

    public Integer[][][] maps;

    public MapGenerator() {    
        try (InputStream inputStream = getClass().getResourceAsStream(MazeGame.MAPS_PATH)) {
            Scanner scan = new Scanner(inputStream);
            ArrayList<Integer[][]> mapsList = new ArrayList<>();

            ArrayList<Integer[]> rows = new ArrayList<>();
            int lastLength = -1;
            while(scan.hasNextLine()) {
                String next = scan.nextLine();
                // System.out.println(next);
                if(next.equals("ENDL")) {
                    lastLength = -1;
                    Integer[][] mapToAdd = new Integer[rows.size()][rows.get(0).length];
                    for(int i = 0; i < rows.size(); i++) {
                        for(int j = 0; j < rows.get(i).length; j++) {
                            mapToAdd[i][j] = rows.get(i)[j];
                        }
                    }
                    mapsList.add(mapToAdd);
                    rows = new ArrayList<>();
                } else {
                    String[] tokens = next.split("");
                    Integer[] row = new Integer[tokens.length];
                    if(lastLength < 0) lastLength = tokens.length;
                    for(int i = 0; i < lastLength; i++) {
                        row[i] = Integer.parseInt(tokens[i]);
                    }
                    rows.add(row);
                }
            }

            maps = new Integer[mapsList.size()][][];
            for(int i = 0; i < mapsList.size(); i++) {
                maps[i] = mapsList.get(i);
            }

            scan.close();
        } catch(Exception e) {
            System.err.println("Failed to read res/maps.txt! Levels will not be read.\n");
            // fnfe.printStackTrace();
            maps = new Integer[][][] {
                {
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
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

    public Integer[][] getMap(int level) {
        return maps[level];
    }

    public Integer[][] generateMaze(int size) {
        if(size < 5) throw new IllegalArgumentException("Argument " + size + " is less than 5");
        Integer[][] maze = new Integer[size][size];

        for(int i = 0; i < maze.length; i++) {
            for(int j = 0; j < maze[i].length; j++) {
                maze[i][j] = 1;
            }
        }

        Coord start = new Coord(1, 1);
        maze[start.getY()][start.getX()] = 0;
        Stack<Coord> coordStack = new Stack<Coord>();
        coordStack.add(start);
        
        dfsGeneration(start, coordStack, maze);

        Coord goalCoord = new Coord(maze.length - 1, maze[maze.length - 1].length - 1);

        while(maze[goalCoord.getY()][goalCoord.getX()] == 1 && goalCoord.getY() > -1 && goalCoord.getX() > -1) {
            if(maze[goalCoord.getY() - 1][goalCoord.getX()] == 0) goalCoord = new Coord(goalCoord.getY() - 1, goalCoord.getX());
            else if(maze[goalCoord.getY()][goalCoord.getX() - 1] == 0) goalCoord = new Coord(goalCoord.getY(), goalCoord.getX() - 1);
            else if(maze[goalCoord.getY() - 1][goalCoord.getX() - 1] == 0) goalCoord = new Coord(goalCoord.getY() - 1, goalCoord.getX() - 1);
        }
        maze[goalCoord.getY()][goalCoord.getX()] = 2;

        return maze;
    }

    private void dfsGeneration(Coord curr, Stack<Coord> coords, Integer[][] maze) {
        if(coords.size() == 0) return;
        ArrayList<Coord> availCoords = getAvailCoords(curr, maze);
        if(availCoords.size() > 0) {
            Coord randCoord = availCoords.get((int)(Math.random() * availCoords.size()));
            maze[randCoord.getY()][randCoord.getX()] = 0;
            coords.push(randCoord);
            dfsGeneration(randCoord, coords, maze);
        } else {
            Coord last = coords.pop();
            dfsGeneration(last, coords, maze);
        }
    }

    private ArrayList<Coord> getAvailCoords(Coord curr, Integer[][] maze) {
        
        ArrayList<Coord> coords = new ArrayList<Coord>();

        int yPos = curr.getY() - 1;
        if(yPos > 0) {
            if(maze[yPos][curr.getX()] != 0 ) {
                if((yPos > 0 && maze[yPos - 1][curr.getX()] == 1) && (curr.getX() > 0 && maze[yPos][curr.getX() - 1] == 1) && (curr.getX() < maze[yPos].length && maze[yPos][curr.getX() + 1] == 1)) {
                    coords.add(new Coord(yPos, curr.getX()));
                }
            }
        }

        yPos = curr.getY() + 1;
        if(yPos < maze.length) {
            if(maze[yPos][curr.getX()] != 0 ) {
                if((yPos < maze.length - 1 && maze[yPos + 1][curr.getX()] == 1) && (curr.getX() > 0 && maze[yPos][curr.getX() - 1] == 1) && (curr.getX() < maze[yPos].length  && maze[yPos][curr.getX() + 1] == 1)) {
                    coords.add(new Coord(yPos, curr.getX()));
                }
            }
        }

        int xPos = curr.getX() - 1;
        if(xPos > 0) {
            if(maze[curr.getY()][xPos] != 0 ) {
                if((xPos > 0 && maze[curr.getY()][xPos - 1] == 1) && (curr.getY() > 0 && maze[curr.getY() - 1][xPos] == 1) && (curr.getY() < maze.length && maze[curr.getY() + 1][xPos] == 1)) {
                    coords.add(new Coord(curr.getY(), xPos));
                }
            }
        }

        xPos = curr.getX() + 1;
        if(xPos < maze[curr.getY()].length) {
            if(maze[curr.getY()][xPos] != 0 ) {
                if((xPos < maze[curr.getY()].length - 1 && maze[curr.getY()][xPos + 1] == 1) && (curr.getY() > 0 && maze[curr.getY() - 1][xPos] == 1) && (curr.getY() < maze.length && maze[curr.getY() + 1][xPos] == 1)) {
                    coords.add(new Coord(curr.getY(), xPos));
                }
            }
        }

        return coords;
    }

    private class Coord {
        private int y, x;
        Coord(int y, int x) {
            this.y = y;
            this.x = x;
        }
        public int getY() { return this.y; }
        public int getX() { return this.x; }
    }

}