import java.awt.*;
import java.awt.image.*;
import java.util.function.*;
import java.util.Random;
import java.awt.geom.*;
import javax.swing.*;

// Recursively fills the given implicit area with randomly subdivided rectangles.

public class ImplicitRenderer extends JPanel {
    
    // The predicate that defines the implicit area (x,y) for which pred.test (x, y) is true.
    private BiPredicate<Double, Double> pred;
    // The size of the viewing area.
    private Rectangle2D.Double box;
    
    public ImplicitRenderer(BiPredicate<Double, Double> pred, int pw, int ph) {
        this.box = new Rectangle2D.Double(0, 0, pw, ph);
        this.pred = pred;
        this.setBackground(Color.WHITE);
        this.setPreferredSize(new Dimension(pw, ph));
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2.0f));
        renderImage(g2, box);
    }
    
    // Render a single rectangle.
    private static Random rng = new Random();
    private static void renderRect(Graphics2D g2, Rectangle2D.Double r) {
        g2.setPaint(Color.BLACK);
        g2.draw(r);
        int c = rng.nextInt(255); // grayscale value
        g2.setPaint(new Color((c << 16) | (c << 8) | c));
        g2.fill(r);
    }
    
    // Recursively render the implicit area that falls into the given box. Returns true if the box is full, that
    // is, every point (within the samples) inside this box is part of the implicit area.
    public boolean renderImage(Graphics2D g2, Rectangle2D.Double box) {
        if(box.getWidth() < 1 && box.getHeight() < 1) {
            return pred.test(box.getX() + box.getWidth() / 2, box.getY() + box.getHeight() / 2);
        }
        // Top left corner of this rectangle
        double x = box.getX();
        double y = box.getY();
        // Choose a random subdivision point inside this rectangle.
        double hw = (0.2 + rng.nextDouble() * 0.6) * box.getWidth();
        double hh = (0.2 + rng.nextDouble() * 0.6) * box.getHeight();
        
        // Recursively calculate the subdivisions. 
        boolean nw = renderImage(g2, new Rectangle2D.Double(x, y, hw, hh));
        boolean ne = renderImage(g2, new Rectangle2D.Double(x + hw, y, box.getWidth() - hw, hh));
        boolean sw = renderImage(g2, new Rectangle2D.Double(x, y + hh, hw, box.getHeight() - hh));
        boolean se = renderImage(g2, new Rectangle2D.Double(x + hw, y + hh, box.getWidth() - hw, box.getHeight() - hh));

        // Render the subdivision rectangles unless they are all full.
        boolean allIn = nw && ne && sw && se;
        if(!allIn) {
            if(nw) { renderRect(g2, new Rectangle2D.Double(x, y, hw, hh)); }
            if(ne) { renderRect(g2, new Rectangle2D.Double(x + hw, y, box.getWidth() - hw, hh)); }
            if(sw) { renderRect(g2, new Rectangle2D.Double(x, y + hh, hw, box.getHeight() - hh)); }
            if(se) { renderRect(g2, new Rectangle2D.Double(x + hw, y + hh, box.getWidth() - hw, box.getHeight() - hh)); }
        }
        return allIn;
    }
    
    // Utility class to convert any Swing Area into an implicit function.
    public static class ImplicitArea implements BiPredicate<Double, Double> {
        private Area ar;
        public ImplicitArea(Area ar) { this.ar = ar; }
        public boolean test(Double x, Double y) {
            return ar.contains(x, y);
        }
    }
    
    // For demonstration purposes, twelve superellipses.
    private static final int MARGIN = 10;
    public static void main(String[] args) {
        JFrame f = new JFrame("Superellipse subdivision demo");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new GridLayout(3, 4));

        // Piet Hein's superellipse: http://en.wikipedia.org/wiki/Superellipse
        class SuperEllipse implements BiPredicate<Double, Double> {
            private double exp, r;
            public SuperEllipse(double exp, double r) {
                this.exp = exp; this.r = r;
            }
            public boolean test(Double x, Double y) {
                double xp = Math.pow(Math.abs(x - r - MARGIN), exp);
                double yp = Math.pow(Math.abs(y - r - MARGIN), exp);
                return xp + yp < Math.pow(r, exp);
            }
        }
        
        for(int i = 0; i < 12; i++) {
            f.add(new ImplicitRenderer(
                new SuperEllipse(1.2 + 0.2 * i, 100), 200 + 2 * MARGIN, 200 + 2 * MARGIN)
            );
        }
        f.pack();
        f.setVisible(true);
        
        // For another cool demo, let's create a complicated Area and render that.
        Area a = new Area(new Ellipse2D.Double(25, 25, 350, 350));
        final int SPOKES = 7;
        a.subtract(new Area(new Ellipse2D.Double(75, 75, 250, 250)));
        for(int i = 0; i < SPOKES; i++) {
            Area b = new Area(new Rectangle2D.Double(200, 185, 190, 30));
            AffineTransform at = AffineTransform.getRotateInstance(
                i * 2 * Math.PI / SPOKES, 200, 200
            );
            b.transform(at);
            a.add(b);
        }
        
        JFrame f2 = new JFrame("Area subdivision demo");
        f2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f2.setLayout(new FlowLayout());
        f2.add(new ImplicitRenderer(new ImplicitArea(a), 400, 400));
        f2.pack();
        f2.setLocation(900, 100);
        f2.setVisible(true);
    }
}
