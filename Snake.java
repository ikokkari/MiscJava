import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * A Java implementation of the classic Nokia phone game of Snake. The snake,
 * controlled by left and right cursor keys, eats food pills and must avoid
 * colliding with its own body.
 * @author Ilkka Kokkarinen
 */

public class Snake extends JPanel {

    private static final int INITLENGTH = 5;
    private static final int TILESIZE = 20;
    private static final int INITPILLS = 5;
    private static final Random rng = new Random();
    
    private enum Tile { EMPTY, SNAKE, PILL }
    private int[][] dir = { {0, 1}, {-1, 0}, {0, -1}, {1, 0} };
    private int gridWidth, gridHeight;
    private Tile[][] field;
    private java.util.List<Point> body;
    private int currentDir, currentLen;
    private javax.swing.Timer timer;
    
    /**
     * Construct a new Snake game of given size.
     * @param gw The width of the grid, in tiles.
     * @param gh The height of the grid, in tiles.
     */
    public Snake(int gw, int gh) {
        this.setBackground(Color.BLACK);
        this.setPreferredSize(new Dimension(TILESIZE * gw, TILESIZE * gh));
        this.gridWidth = gw;
        this.gridHeight = gh;
        field = new Tile[gw][gh];
        this.timer = new javax.swing.Timer(100, new MyTimerListener());
        timer.start();
        this.addKeyListener(new MyKeyListener());
        this.setFocusable(true);
        this.requestFocus();
        startNewGame();
    }
    
    private void placeRandomPill() {
        int x, y;
        do {
            x = rng.nextInt(gridWidth);
            y = rng.nextInt(gridHeight);
        } while(field[x][y] != Tile.EMPTY);
        field[x][y] = Tile.PILL;
    }
    
    private void startNewGame() {
        for(int x = 0; x < gridWidth; x++) {
            for(int y = 0; y < gridHeight; y++) {
                field[x][y] = Tile.EMPTY;
            }
        }
        body = new LinkedList<Point>();
        for(int i = 0; i < INITLENGTH; i++) {
            int x = gridWidth / 2;
            int y = i + (gridHeight - INITLENGTH) / 2;
            field[x][y] = Tile.SNAKE;
            body.add(new Point(x, y));
        }
        currentDir = 0;
        currentLen = INITLENGTH;
        for(int i = 0; i < INITPILLS; i++) { placeRandomPill(); }
    }
    
    private class MyTimerListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            Point head = body.get(body.size() - 1);
            int hx = (int)head.getX();
            int hy = (int)head.getY();
            int nx = (hx + dir[currentDir][0]);
            if(nx < 0) { nx = gridWidth - 1; }
            if(nx >= gridWidth) { nx = 0; }
            int ny = (hy + dir[currentDir][1]);
            if(ny < 0) { ny = gridHeight - 1; }
            if(ny >= gridHeight) { ny = 0; }
            
            if(field[nx][ny] == Tile.SNAKE) {
                startNewGame();
                return;
            }
            else if(field[nx][ny] == Tile.PILL) {
                currentLen += rng.nextInt(10) + 5;
                placeRandomPill();
            }
            field[nx][ny] = Tile.SNAKE;
            body.add(new Point(nx, ny));
            if(body.size() > currentLen) {
                Point tail = body.remove(0);
                field[(int)tail.getX()][(int)tail.getY()] = Tile.EMPTY;
            }
            repaint();
        }
    }
    
    private class MyKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent ke) {
            int kc = ke.getKeyCode();
            if(kc == KeyEvent.VK_LEFT) {
                if(--currentDir < 0) { currentDir = 3; }
            }
            if(kc == KeyEvent.VK_RIGHT) {
                if(++currentDir > 3) { currentDir = 0; }
            }
        }
    }
    
    /**
     * Render this component as it currently looks like.
     * @param g The {@code Graphics} object provided by Swing for us to draw on.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
          
        for(int x = 0; x < gridWidth; x++) {
            for(int y = 0; y < gridHeight; y++) {
                Color c;
                switch(field[x][y]) {
                    case EMPTY: c = Color.BLACK; break;
                    case SNAKE: c = Color.GRAY; break;
                    case PILL: c = Color.GREEN; break;
                    default: c = Color.BLACK; break;
                }
                g2.setPaint(c);
                g2.fill(new Rectangle2D.Double(TILESIZE * x + 1, TILESIZE * y + 1, 
                TILESIZE - 2, TILESIZE - 2));
            }
        }
    }
    
    /**
     * Terminate the internal animation timer of the game so that the JVM can terminate.
     */
    public void terminate() {
        timer.stop();
        System.out.println("Snake timer terminated");
    }
    
    public static void main(String[] args) {
        final JFrame f = new JFrame("Snake!");
        final Snake sn = new Snake(30, 30);
        f.add(sn);
        // Must ensure that timer is stopped when the Frame closes
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                sn.terminate();
                f.dispose();
            }
        });
        f.pack();
        f.setVisible(true);        
    }
}
