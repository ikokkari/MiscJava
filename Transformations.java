import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.geom.*;

/**
 * Demonstrate the affine transformations (translate, scale, rotate) for Swing
 * geometric {@Shape} objects decorated with the {@Area} decorator.
 * @author Ilkka Kokkarinen
 */

public class Transformations extends JPanel {
    private JSlider scale, rotate;

    public Transformations() {

        // Swing sliders generate change events, not action or item events
        class MyChangeListener implements ChangeListener {
            public void stateChanged(ChangeEvent ce) { repaint(); }
        }

        scale = new JSlider(0, 100);
        scale.setValue(50);
        scale.addChangeListener(new MyChangeListener());

        rotate = new JSlider(0, 360);
        rotate.setValue(0);
        rotate.addChangeListener(new MyChangeListener());

        this.setLayout(new FlowLayout());
        this.add(new JLabel("Scale"));
        this.add(scale);
        this.add(new JLabel("Rotate"));
        this.add(rotate);
        this.setPreferredSize(new Dimension(500,500));
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

        // Define the shape to work with
        Area myShape = new Area(new Rectangle2D.Double(-50,-50,100,100));
        myShape.add(new Area(new Ellipse2D.Double(-60,-60,120,120)));
        myShape.subtract(new Area(new Ellipse2D.Double(-40,-40,80,80)));
        // Transform it according to what the sliders show
        AffineTransform at = new AffineTransform();
        at.translate(250,250);
        at.rotate(Math.toRadians(rotate.getValue()));
        at.scale(scale.getValue() / 50.0, scale.getValue() / 50.0);
        // The previous transformations need to be done in reverse order of how they happen.
        myShape.transform(at); // Transform the shape
        g2.setStroke(new BasicStroke(5.0f));
        g2.setPaint(Color.RED);
        g2.draw(myShape);
        g2.setPaint(Color.BLUE);
        g2.fill(myShape);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Transformations demo");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new FlowLayout());
        f.add(new Transformations());
        f.pack();
        f.setVisible(true);        
    }    
}
