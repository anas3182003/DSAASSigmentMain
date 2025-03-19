package maze;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Maze extends JFrame {

    // Constants for cell types
    final static int WALL = 1;
    final static int PATH = 0;
    final static int START = 2;
    final static int GOAL = 8;
    final static int VISITED = 9;

    // Starting position and goal position
    final static int START_I = 1, START_J = 1;
    final static int END_I = 2, END_J = 9;

    // The maze grid
    int[][] maze = new int[][]{
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 1, 0, 1, 1, 0, 8},
        {1, 0, 1, 1, 1, 0, 1, 1, 0, 1},
        {1, 0, 0, 0, 0, 1, 1, 1, 0, 1},
        {1, 1, 1, 1, 0, 1, 1, 1, 0, 1},
        {1, 1, 1, 1, 0, 1, 0, 0, 0, 1},
        {1, 1, 0, 1, 0, 1, 1, 0, 0, 1},
        {1, 1, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 0, 1, 1, 1}
    };

    // For random maze generation
    int[][] randomMaze;

    // UI Components
    private JButton solveStackButton, solveBFSButton, clearButton, exitButton, genRandomButton;
    private JLabel elapsedDfsLabel, elapsedBFSLabel;
    private JTextField textDfs, textBFS;
    
    private boolean repaint = false;
    
    // Timing variables
    private long startTime, stopTime, duration;
    private double dfsTime, bfsTime;
    
    // Backup of the original maze
    private final int[][] originalMaze = cloneMaze();

    public Maze() {
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        // Basic frame setup
        setTitle("Maze");
        setSize(960, 530);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        
        // Set icon if available
        URL urlIcon = getClass().getResource("flat-theme-action-maze-icon.png");
        if (urlIcon != null) {
            setIconImage(new ImageIcon(urlIcon).getImage());
        }
        
        // Initialize UI components
        solveStackButton = new JButton("Solve DFS");
        solveBFSButton = new JButton("Solve BFS");
        clearButton = new JButton("Clear");
        exitButton = new JButton("Exit");
        genRandomButton = new JButton("Generate Random Maze");
        
        elapsedDfsLabel = new JLabel("Elapsed Time :");
        elapsedBFSLabel = new JLabel("Elapsed Time :");
        textDfs = new JTextField();
        textBFS = new JTextField();
        
        // Position components
        solveStackButton.setBounds(500, 50, 100, 40);
        solveBFSButton.setBounds(630, 50, 100, 40);
        clearButton.setBounds(760, 50, 100, 40);
        exitButton.setBounds(760, 115, 100, 40);
        elapsedDfsLabel.setBounds(500, 100, 100, 40);
        genRandomButton.setBounds(500, 180, 170, 40);
        elapsedBFSLabel.setBounds(630, 100, 100, 40);
        textDfs.setBounds(500, 130, 100, 25);
        textBFS.setBounds(630, 130, 100, 25);
        
        // Add components to the frame
        add(solveStackButton);
        add(solveBFSButton);
        add(clearButton);
        add(elapsedDfsLabel);
        add(textDfs);
        add(elapsedBFSLabel);
        add(textBFS);
        add(exitButton);
        add(genRandomButton);
        
        setVisible(true);
    }
    
    private void setupEventHandlers() {
        // Generate Random Maze Button
        genRandomButton.addActionListener((ActionEvent e) -> {
            int[][] newMaze = generateRandomMaze();
            repaint = true;
            restoreMaze(newMaze);
            repaint();
        });
        
        // Exit Button
        exitButton.addActionListener((ActionEvent e) -> System.exit(0));
        
        // Clear Button
        clearButton.addActionListener((ActionEvent e) -> {
            repaint = true;
            if (randomMaze == null) {
                restoreMaze(originalMaze);
            } else {
                restoreMaze(randomMaze);
            }
            textBFS.setText("");
            textDfs.setText("");
            repaint();
        });
        
        // Solve DFS Button
        solveStackButton.addActionListener((ActionEvent e) -> {
            if (randomMaze == null) {
                restoreMaze(originalMaze);
            } else {
                restoreMaze(randomMaze);
            }
            repaint = false;
            solveDFS();
            repaint();
        });
        
        // Solve BFS Button
        solveBFSButton.addActionListener((ActionEvent e) -> {
            if (randomMaze == null) {
                restoreMaze(originalMaze);
            } else {
                restoreMaze(randomMaze);
            }
            repaint = false;
            solveBFS();
            repaint();
        });
    }
    
    // Get maze size - renamed to avoid conflict with Component.getSize()
    private int getMazeSize() {
        return maze.length;
    }
    
    // Print maze to console for debugging
    private void printMaze() {
        for (int i = 0; i < getMazeSize(); i++) {
            for (int j = 0; j < getMazeSize(); j++) {
                System.out.print(maze[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    // Check if position is within maze boundaries
    private boolean isInMaze(int i, int j) {
        return i >= 0 && i < getMazeSize() && j >= 0 && j < getMazeSize();
    }
    
    private boolean isInMaze(MazePos pos) {
        return isInMaze(pos.i(), pos.j());
    }
    
    // Mark a position with a value and return previous value
    private int mark(int i, int j, int value) {
        if (!isInMaze(i, j)) {
            throw new AssertionError("Position outside maze: " + i + "," + j);
        }
        int temp = maze[i][j];
        maze[i][j] = value;
        return temp;
    }
    
    private int mark(MazePos pos, int value) {
        return mark(pos.i(), pos.j(), value);
    }
    
    // Check if a position is marked as visited
    private boolean isMarked(int i, int j) {
        if (!isInMaze(i, j)) {
            throw new AssertionError("Position outside maze: " + i + "," + j);
        }
        return maze[i][j] == VISITED;
    }
    
    private boolean isMarked(MazePos pos) {
        return isMarked(pos.i(), pos.j());
    }
    
    // Check if a position is traversable (not a wall and not visited)
    private boolean isTraversable(int i, int j) {
        if (!isInMaze(i, j)) {
            throw new AssertionError("Position outside maze: " + i + "," + j);
        }
        return maze[i][j] != WALL && maze[i][j] != VISITED;
    }
    
    private boolean isTraversable(MazePos pos) {
        return isTraversable(pos.i(), pos.j());
    }
    
    // Check if position is the goal
    private boolean isGoal(int i, int j) {
        return i == END_I && j == END_J;
    }
    
    private boolean isGoal(MazePos pos) {
        return isGoal(pos.i(), pos.j());
    }
    
    // Create a copy of the current maze
    private int[][] cloneMaze() {
        int[][] mazeCopy = new int[getMazeSize()][getMazeSize()];
        for (int i = 0; i < getMazeSize(); i++) {
            System.arraycopy(maze[i], 0, mazeCopy[i], 0, getMazeSize());
        }
        return mazeCopy;
    }
    
    // Restore maze from a saved state
    private void restoreMaze(int[][] savedMaze) {
        for (int i = 0; i < getMazeSize(); i++) {
            System.arraycopy(savedMaze[i], 0, maze[i], 0, getMazeSize());
        }
        
        // Ensure start and goal positions have correct values
        maze[START_I][START_J] = START;
        maze[END_I][END_J] = GOAL;
    }
    
    // Generate a random maze with guaranteed path from start to end
    private int[][] generateRandomMaze() {
        randomMaze = new int[10][10];
        Random rnd = new Random();
        
        // Fill with walls initially
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                randomMaze[i][j] = WALL;
            }
        }
        
        // Clear interior cells
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                randomMaze[i][j] = PATH;
            }
        }
        
        // Set start and goal
        randomMaze[START_I][START_J] = START;
        randomMaze[END_I][END_J] = GOAL;
        
        // Add some random walls (30% chance)
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                // Skip start and end positions
                if ((i == START_I && j == START_J) || (i == END_I && j == END_J)) {
                    continue;
                }
                
                if (rnd.nextDouble() < 0.3) {
                    randomMaze[i][j] = WALL;
                }
            }
        }
        
        // Ensure there's a path from start to end
        boolean pathExists = checkPathExists(randomMaze.clone());
        
        // If no path exists, clear walls until there is a path
        while (!pathExists) {
            // Pick a random wall to remove
            int i = rnd.nextInt(8) + 1;
            int j = rnd.nextInt(8) + 1;
            
            // Make sure it's not start or end position and is currently a wall
            if (!((i == START_I && j == START_J) || (i == END_I && j == END_J)) && randomMaze[i][j] == WALL) {
                randomMaze[i][j] = PATH;
                
                // Check if path exists now
                pathExists = checkPathExists(randomMaze.clone());
            }
        }
        
        // Final check to ensure start and end are correctly set
        randomMaze[START_I][START_J] = START;
        randomMaze[END_I][END_J] = GOAL;
        
        return randomMaze;
    }
    
    // Check if a path exists from start to end using BFS
    private boolean checkPathExists(int[][] testMaze) {
        boolean[][] visited = new boolean[10][10];
        LinkedList<int[]> queue = new LinkedList<>();
        
        // Add start position to queue
        queue.add(new int[]{START_I, START_J});
        visited[START_I][START_J] = true;
        
        // Possible moves (up, right, down, left)
        int[][] moves = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int i = current[0];
            int j = current[1];
            
            // Check if we've reached the end
            if (i == END_I && j == END_J) {
                return true;
            }
            
            // Try all four directions
            for (int[] move : moves) {
                int newI = i + move[0];
                int newJ = j + move[1];
                
                // Check if the new position is valid and not a wall and not visited
                if (newI >= 0 && newI < 10 && newJ >= 0 && newJ < 10 
                    && testMaze[newI][newJ] != WALL && !visited[newI][newJ]) {
                    
                    visited[newI][newJ] = true;
                    queue.add(new int[]{newI, newJ});
                }
            }
        }
        
        // If we get here, no path was found
        return false;
    }
    
    // Depth-First Search algorithm to solve maze
    private void solveDFS() {
        startTime = System.nanoTime();
        
        Stack<MazePos> stack = new Stack<>();
        stack.push(new MazePos(START_I, START_J));
        
        MazePos current;
        boolean foundGoal = false;
        
        while (!stack.empty()) {
            current = stack.pop();
            
            if (isGoal(current)) {
                foundGoal = true;
                break;
            }
            
            mark(current, VISITED);
            
            // Check all four directions (north, east, west, south)
            MazePos[] neighbors = {
                current.north(), 
                current.east(), 
                current.west(), 
                current.south()
            };
            
            for (MazePos neighbor : neighbors) {
                if (isInMaze(neighbor) && isTraversable(neighbor)) {
                    stack.push(neighbor);
                }
            }
        }
        
        stopTime = System.nanoTime();
        duration = stopTime - startTime;
        dfsTime = (double) duration / 1000000;
        
        textDfs.setText(String.format("%.3f ms", dfsTime));
        
        if (foundGoal) {
            JOptionPane.showMessageDialog(this, "Path found with DFS!");
        } else {
            JOptionPane.showMessageDialog(this, "No path found with DFS.");
        }
        
        System.out.println("\nDFS Result:");
        printMaze();
        System.out.println(String.format("Time: %.3f ms", dfsTime));
    }
    
    // Breadth-First Search algorithm to solve maze
    private void solveBFS() {
        startTime = System.nanoTime();
        
        LinkedList<MazePos> queue = new LinkedList<>();
        queue.add(new MazePos(START_I, START_J));
        
        MazePos current;
        boolean foundGoal = false;
        
        while (!queue.isEmpty()) {
            current = queue.removeFirst();
            
            if (isGoal(current)) {
                foundGoal = true;
                break;
            }
            
            mark(current, VISITED);
            
            // Check all four directions (north, east, west, south)
            MazePos[] neighbors = {
                current.north(), 
                current.east(), 
                current.west(), 
                current.south()
            };
            
            for (MazePos neighbor : neighbors) {
                if (isInMaze(neighbor) && isTraversable(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        
        stopTime = System.nanoTime();
        duration = stopTime - startTime;
        bfsTime = (double) duration / 1000000;
        
        textBFS.setText(String.format("%.3f ms", bfsTime));
        
        if (foundGoal) {
            JOptionPane.showMessageDialog(this, "Path found with BFS!");
        } else {
            JOptionPane.showMessageDialog(this, "No path found with BFS.");
        }
        
        System.out.println("\nBFS Result:");
        printMaze();
        System.out.println(String.format("Time: %.3f ms", bfsTime));
    }
    
    // Paint method to draw the maze on the frame
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.translate(70, 70);
        
        // Cell size
        final int cellSize = 40;
        
        for (int row = 0; row < getMazeSize(); row++) {
            for (int col = 0; col < getMazeSize(); col++) {
                // Choose color based on cell type
                Color cellColor;
                switch (maze[row][col]) {
                    case WALL:
                        cellColor = Color.darkGray;
                        break;
                    case GOAL:
                        cellColor = Color.RED;
                        break;
                    case START:
                        cellColor = Color.YELLOW;
                        break;
                    case VISITED:
                        cellColor = repaint ? Color.WHITE : Color.GREEN;
                        break;
                    default:
                        cellColor = Color.WHITE;
                }
                
                // Fill and draw the cell
                g.setColor(cellColor);
                g.fillRect(cellSize * col, cellSize * row, cellSize, cellSize);
                g.setColor(Color.BLUE);
                g.drawRect(cellSize * col, cellSize * row, cellSize, cellSize);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Maze());
    }
}

// Helper class for maze positions
class MazePos {
    private final int i, j;
    
    public MazePos(int i, int j) {
        this.i = i;
        this.j = j;
    }
    
    public int i() { return i; }
    public int j() { return j; }
    
    public MazePos north() { return new MazePos(i - 1, j); }
    public MazePos south() { return new MazePos(i + 1, j); }
    public MazePos east() { return new MazePos(i, j + 1); }
    public MazePos west() { return new MazePos(i, j - 1); }
    
    @Override
    public String toString() {
        return "(" + i + "," + j + ")";
    }
}