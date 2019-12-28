import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

/**
 * Play around with controlled randomness by dropping pebbles on a two-dimensional
 * grid, each pebble randomly falling down to a neighbour with a lower height.
 */
public class AvalancheWorld {

    private static Random rng = new Random();
    private static int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}, {1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
    
    /**
     * Create a two-dimensional array by dropping a number of pebbles.
     * @param step The range of random step. Should be positive.
     * @param size The size of the two-dimensional array to fill.
     * @param n The number of pebbles to drop.
     * @param prob The probability of a pebble falling into a lower neighbout.
     * @return An array containing the heightfields of the pebbles.
     */
    public static int[][] avalancheWorld(int step, int size, int n, double prob) {
        return avalancheWorld(step, size, n, prob, rng);
    }
    
    /**
     * Create a two-dimensional array by dropping a number of pebbles.
     * @param step The range of random step. Should be positive.
     * @param size The size of the two-dimensional array to fill.
     * @param n The number of pebbles to drop.
     * @param prob The probability of a pebble falling into a lower neighbout.
     * @param rng The random number generator to use.
     * @return An array containing the heightfields of the pebbles.
     */
    public static int[][] avalancheWorld(int step, int size, int n, double prob, Random rng) {
        int[][] ht = new int[size][size];
        int x = rng.nextInt(size);
        int y = rng.nextInt(size);
        for(int i = 0; i < n; i++) {
            ht[x][y]++;
            int d = rng.nextInt(8);
            int xn = (x + dirs[d][0]) % size;
            if(xn < 0) { xn = size + xn; }
            int yn = (y + dirs[d][1]) % size;
            if(yn < 0) { yn = size + yn; }
            if(rng.nextDouble() < prob * (ht[x][y] - ht[xn][yn])) {
                ht[x][y]--; ht[xn][yn]++;
                x = (xn + rng.nextInt(2 * step) - step) % size;
                if(x < 0) { x = size + x; }
                y = (yn + rng.nextInt(2 * step) - step) % size;
                if(y < 0) { y = size + y; }
            }
        }
        return ht;
    }
    
    /**
     * Create a panel that displays the random pebble heightfield in grayscale.
     * @param step The range of random step. Should be positive.
     * @param size The size of the two-dimensional array to fill.
     * @param n The number of pebbles to drop.
     * @param prob The probability of a pebble falling into a lower neighbout.
     * @param rng The random number generator to use.
     * @param hmax The cutoff of height of individual point.
     * @return An array containing the heightfields of the pebbles.
     */
    public static JPanel createPanel(final int step, final int size, int n, final double prob, Random rng, int hmax) {
        final BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        final int[][] ht = avalancheWorld(step, size, n, prob, rng);
        int[] cols = new int[hmax + 1];
        for(int h = 0; h <= hmax; h++) {
            float hn = 1 - ((float)(hmax - h) / (float)hmax);
            cols[h] = new Color(hn, hn, hn).getRGB();
        }
        for(int x = 0; x < size; x++) {
            for(int y = 0; y < size; y++) {
                img.setRGB(x, y, cols[Math.min(ht[x][y], hmax)]);
            }
        }
        
        // Define a local class from which we create an object to return as result.
        class AvalanchePanel extends JPanel {
            public AvalanchePanel() {
                this.setPreferredSize(new Dimension(size + 4, size + 4));
                this.setToolTipText("prob = " + prob + ", step = " + step);
            }
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(img, 2, 2, this);
            }
        }
        
        return new AvalanchePanel();
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame("Avalanche World");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new GridLayout(3, 4));
        for(int x = 0; x < 3; x++) {
            for(int y = 0; y < 4; y++) {
                f.add(createPanel(2*x+1, 300, 300*300*4, 1-0.25*y, new Random(12345), 10));
            }
        }
        f.pack();
        f.setVisible(true);
    }
}
