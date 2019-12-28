import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * Compute the Peter de Jong attractors, as in {@code http://paulbourke.net/fractals/peterdejong/}.
 */

public class DeJong {

    private static final int COLORRES = 255;
    private static Color[] colors;
    // A static initializer block: executed once when class bytecode is loaded. Here we
    // precompute the colours that we are going to use in the rendering.
    static {
        colors = new Color[COLORRES+1];
        for(int i = 0; i < COLORRES+1; i++) {
            colors[i] = new Color(i, i, i);
        }
    }
    
    private static final int MARGIN = 20;
    
    /**
     * Compute the DeJong attractor for given size and parameters.
     * @param w The size of the result image, measured in pixels.
     * @param n The number of points to iterate.
     * @param args The four {@code double} parameters of De Jong equations.
     * @return A {@code BufferedImage} instance to display the result of iteration.
     */
    public static BufferedImage computeDeJong(int w, long n, double[] args) {
        BufferedImage img = new BufferedImage(w+MARGIN, w+MARGIN, BufferedImage.TYPE_INT_RGB);
        int[][] visit = new int[w][w]; // visit counters for each pixel
        int maxVisit = 0; // maximum visit count we have encountered so far
        double cx = 0.1, cy = 0.1; // starting point, doesn't matter much
        
        for(long i = 0; i < n; i++) {
            // Calculate the next point using DeJong formulas.
            double newX = Math.sin(args[0] * cy) - Math.cos(args[1] * cx);
            double newY = Math.sin(args[2] * cx) - Math.cos(args[3] * cy);
            cx = newX; cy = newY;
            // Which pixel does this point fall in?
            int px = (int)Math.floor((((cx + 2) / 4) * w));
            int py = (int)Math.floor((((cy + 2) / 4) * w));
            // Increment its visit counter.
            if(++visit[px][py] > maxVisit) {
                maxVisit = visit[px][py];
            }
        }
        
        double mv = (double)maxVisit;
        for(int x = 0; x < w; x++) {
            for(int y = 0; y < w; y++) {
                // Compute each colour from its visit count percentage of maximum
                // visit count, scaled with the power function to increase contrast.
                double v = Math.pow(visit[x][y] / mv, 0.25);
                // Pick the colour and set the pixel value.
                int ci = (int)(Math.floor(v * COLORRES));
                img.setRGB(y + MARGIN/2, x + MARGIN/2, colors[ci].getRGB());
            }
        }
        
        return img;
    }
    
    /**
     * A utility method to create a JPanel that displays the computed fractal.
     * @param w The size of the result image, measured in pixels.
     * @param n The number of points to iterate.
     * @param args The four {@code double} parameters of De Jong equations.
     * @return A {@code JPanel} instance customized to display the result of iteration.
     */
    public static JPanel createPanel(final int w, long n, double[] args) {
        final BufferedImage img = computeDeJong(w, n, args);
        
        // Define a local class from which we create an object to return as result.
        class DeJongPanel extends JPanel {
            public DeJongPanel() {
                this.setPreferredSize(new Dimension(w + MARGIN, w + MARGIN));
            }
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(img, 0, 0, this);
            }
        }
        
        return new DeJongPanel();
    }
    
    public static void main(String[] args) {
        double[] a = { 1.4, -2.3, 2.4, -2.1 }; // from the page, just to check
        JPanel dp = createPanel(800, 100_000_000L, a); 
        JFrame f = new JFrame("DeJong");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new FlowLayout());
        f.add(dp);
        f.pack();
        f.setVisible(true);
    }
}
