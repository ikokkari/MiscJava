import java.util.*;
import java.security.*;
import java.math.*;

public class KahanSummation {

    private static SecureRandom srng;
    static {
        try {
            srng = SecureRandom.getInstance("SHA1PRNG");
            srng.setSeed("Let's see much better Kahan addition is".getBytes());
        } catch(Exception e) { }
    }

    public interface Adder {
        public double add(double x);
    }

    public class RawAdder implements Adder {
        private double sum = 0.0;
        public double add(double x) {
            sum += x;
            return sum;
        }
    }

    public class KahanAdder implements Adder {
        private double sum = 0.0; // The sum so far
        private double c = 0.0; // How much this sum is off
        public double add(double x) {
            // Instead of adding x, let's add x - c
            double y = x - c;
            // What we get when we add that
            double t = sum + y;
            // How much we are off with the new sum
            c = (t - sum) - y;
            sum = t;
            return sum;
        }
    }
    
    public class BigDecimalAdder implements Adder {
        private MathContext mc = new MathContext(50);
        private BigDecimal sum = new BigDecimal(0, mc);
        public double add(double x) {
            sum = sum.add(new BigDecimal(x, mc));
            return sum.doubleValue();
        }
    }

    public void test(int n, int maxExp) {
        List<Double> c = new ArrayList<Double>(n);
        for(int i = 0; i < n; i++) {
            double mantissa = srng.nextDouble() + 1;
            double exponent = srng.nextDouble() * 2 * maxExp - maxExp;
            double x = mantissa * Math.pow(10, exponent);
            c.add(x);
            c.add(-x);
        }
        // Shuffling doesn't change the fact that elements add up to zero.
        Collections.shuffle(c);
        // Try out all three adders with exact same data and see what happens.
        Adder rawAdder = new RawAdder();
        Adder kahanAdder = new KahanAdder();
        Adder bdAdder = new BigDecimalAdder();
        for(double x: c) {
            rawAdder.add(x);
            kahanAdder.add(x);
            bdAdder.add(x);
        }
        System.out.println("Raw adder error  : " + Math.abs(rawAdder.add(0)));
        System.out.println("Kahan adder error: " + Math.abs(kahanAdder.add(0)));
        System.out.println("BD adder error   : " + Math.abs(bdAdder.add(0)));
    } 
}
