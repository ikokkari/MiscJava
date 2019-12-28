import java.util.*;
import java.util.function.*;

// Demonstrate the ColouringSolver by using it to solve a Sudoku problem.

public class SudokuColour {

    // The index of tile in coordinates (x, y).
    private static int getIdx(int x, int y) {
        return 9 * y + x;
    }
    // The x-coordinate of tile of index idx.
    private static int getX(int idx) { return idx % 9; }
    // The y-coordinate of tile of index idx.
    private static int getY(int idx) { return idx / 9; }
    // The block number of tile of index idx.
    private static int getB(int idx) { 
        int dx = getX(idx) / 3;
        int dy = getY(idx) / 3;
        return 3 * dx + dy;
    }
    // List of neighbours for Sudoku tiles, precomputed below.
    private static List<List<Integer>> neighbours = new ArrayList<List<Integer>>(81);
    
    // Executed at class initialization: precompute the list of neighbours for each tile.
    static {
        for(int i = 0; i < 81; i++) {
            neighbours.add(new ArrayList<Integer>());
        }
        for(int i = 0; i < 81; i++) {
            for(int j = i + 1; j < 81; j++) {
                if(i == j) { continue; } // No tile is its own neighbour.
                // Tiles with same x, same y or same block number are neighbours.
                if(getX(i) == getX(j) || getY(i) == getY(j) || getB(i) == getB(j)) {
                    neighbours.get(i).add(j);
                    neighbours.get(j).add(i);
                }
            }
        }
    }
    
    /** Solve the Sudoku puzzle with the given 9*9 board.
     * @param board The 9*9 integer array that contains the puzzle, with 0 denoting an empty tile.
     * @return Truth value telling if search was successful, in which case the solution is in the {@code board} array.
     */
    public static boolean solve(int[][] board) {
        // Calculate the domains of each variable based on the initial board.
        List<List<Integer>> domain = new ArrayList<List<Integer>>(81);
        for(int i = 0; i < 81; i++) {
            List<Integer> dom = new ArrayList<Integer>();
            int e = board[getX(i)][getY(i)];
            // A tile without a value has the numbers 1 to 9 in its initial domain.
            if(e == 0) {
                for(int j = 1; j <= 9; j++) {
                    dom.add(j);
                }
            }
            // If the tile has been given a value, that value is its singleton domain.
            else { dom.add(e); }
            domain.add(dom);
        }
        long startTime = System.currentTimeMillis();
        // Find the solution using graph colouring solver.
        List<Integer> solution = ColouringSolver.solve(
            81, 
            i -> neighbours.get(i),
            i -> domain.get(i)
            );
        long endTime = System.currentTimeMillis();
        System.out.println("Solution found in " + (endTime - startTime) + " ms. Call count is "
        + ColouringSolver.getCallCount() + ", forward checking cutoff count is "+ ColouringSolver.getFcCount() + ".");
        // An invalid puzzle has no solutions.
        if(solution == null) { return false; }
        // Otherwise, fill in the board array from the solution. 
        for(int i = 0; i < 81; i++) {
            board[getX(i)][getY(i)] = solution.get(i);
        }
        return true;
    }
    
    private static void printBoard(int[][] board) {
        for(int x = 0; x < 9; x++) {
            for(int y = 0; y < 9; y++) {
                System.out.print(board[x][y] + " ");
            }
            System.out.println("");
        }
    }
    
    public static void main(String[] args) {
        // https://puzzling.stackexchange.com/questions/305/why-is-this-considered-to-be-the-worlds-hardest-sudoku
        int[][] testBoard = {
            {8,0,0,0,0,0,0,0,0},
            {0,0,3,6,0,0,0,0,0},
            {0,7,0,0,9,0,2,0,0},

            {0,5,0,0,0,7,0,0,0},
            {0,0,0,0,4,5,7,0,0},
            {0,0,0,1,0,0,0,3,0},

            {0,0,1,0,0,0,0,6,8},
            {0,0,8,5,0,0,0,1,0},
            {0,9,0,0,0,0,4,0,0}
            };  

        solve(testBoard);
        printBoard(testBoard);
    }
    
}
