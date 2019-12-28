import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.util.Random;

// Inspired by http://nifty.stanford.edu/2009/stone-random-art/
// For a far more complex implementation, see the instructor's hobby project
// http://www.scs.ryerson.ca/~ikokkari/RandomArt/RandomArt.html

public class PixelArt {

    private static enum Op {
        AVG, SIN, COS, PROD, SIGMOID, SQRT, NEG;
        
        // Not the cleanest way to do this, but suitable for CCPS109.
        public double evaluate(double a1, double a2) {
            switch(this) {
                case AVG:
                    return (a1 + a2) / 2;
                case SIN:
                    return Math.sin(Math.PI * a1 + a2);
                case COS:
                    return Math.cos(Math.PI * a1 + a2);
                case PROD:
                    return a1*a2;
                case SIGMOID:
                    return 2 * (1.0 / (1.0 + Math.exp(-10 * a1))) - 1.0;
                case SQRT:
                    return (a1 > 0? +1: -1) * Math.sqrt(Math.abs(a1));
                case NEG:
                    return -a1;
                default:
                    return 0;
            }
        }
    }
    
    private static Random rng = new Random();
    
    // Create a function randomly.
    private static int[][] createFunction(int len) {
        int[][] res = new int[len + 2][3];
        for(int i = 2; i < len + 2; i++) {
            res[i][0] = rng.nextInt(Op.values().length);
            int min = i > 6? i - 6: 0; 
            res[i][1] = min + rng.nextInt(i - min);
            res[i][2] = min + rng.nextInt(i - min);
        }
        return res;
    }
    
    // Evaluate the function at point (x, y).
    private static double evaluate(int[][] func, double x, double y) {
        double[] vals = new double[func.length];
        vals[0] = x;
        vals[1] = y;
        for(int i = 2; i < func.length; i++) {
            vals[i] = Op.values()[func[i][0]].evaluate(vals[func[i][1]], vals[func[i][2]]);
        }
        return vals[func.length-1];
    }
    
    // Create a random art image of given size and function length.
    public static BufferedImage computeArt(int size, int len, boolean HSB) {
        int[][] rf = createFunction(len); // red channel
        int[][] gf = createFunction(len); // green channel
        int[][] bf = createFunction(len); // blue channel
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for(int px = 0; px < size; px++) {
            for(int py = 0; py < size; py++) {
                double x = 2*(px / (double)size) - 1;
                double y = 2*(py / (double)size) - 1;
                double r = (evaluate(rf, x, y) + 1) / 2;
                double g = (evaluate(gf, x, y) + 1) / 2;
                double b = (evaluate(bf, x, y) + 1) / 2;
                
                if(HSB) { // treat as HSB colour triple
                    img.setRGB(px, py, Color.HSBtoRGB((float)r, (float)g, (float)b));
                }
                else { // treat as RGB colour triple
                    int ri = (int)(254 * r);
                    int gi = (int)(254 * g);
                    int bi = (int)(254 * b);
                    img.setRGB(px, py, (ri << 16) | (gi << 8) | bi);
                }
            }
        }
        return img;
    }

    private static final int MARGIN = 10;
    public static JPanel createPanel(final int size, final int len) {

        // Define a local class from which we create an object to return as result.
        class ArtPanel extends JPanel {
            private BufferedImage img;
            private boolean HSB = false;
            public ArtPanel() {
                img = computeArt(size, len, HSB);
                this.setPreferredSize(new Dimension(size + 2*MARGIN, size+2*MARGIN));
                this.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent me) {
                        if(me.getButton() == MouseEvent.BUTTON1) { 
                            img = computeArt(size, len, HSB);
                            repaint();
                        }
                        else {
                            HSB = !HSB; 
                        }
                    }
                });
            }
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(img, MARGIN, MARGIN, this);
            }
        }
        return new ArtPanel();
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame("Random pixel art");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new GridLayout(2, 2));
        for(int i = 0; i < 4; i++) {
            f.add(createPanel(400, 30));
        }
        f.pack();
        f.setVisible(true);        
    }
}
