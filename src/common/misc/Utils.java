package common.misc;

import java.util.Random;

final public class Utils {
    public static final double Pi = Math.PI;
    public static final double TwoPi = Math.PI * 2;
    public static final double QuarterPi = Math.PI / 4;
    public static final double EpsilonDouble = Double.MIN_NORMAL;

    private static Random rand = new Random();
    private static double y2 = 0;
    private static boolean use_last = false;

    /**
     * returns true if the value is a NaN
     */
    public static <T> boolean isNaN(T val) {
        return !(val != null);
    }

    public static double degsToRads(double degs) {
        return TwoPi * (degs / 360.0);
    }
    
    public static boolean isEqual(float a, float b) {
        return Math.abs(a - b) < 1E-12;
    }

    public static boolean isEqual(double a, double b) {
        return Math.abs(a - b) < 1E-12;
    }

    //-----------------------------------------------------------------------
    //  some random functions
    //-----------------------------------------------------------------------

    public static void setSeed(long seed) {
        rand.setSeed(seed);
    }

    /**
     * @return a random integer between x and y
     */
    public static int randInt(int x, int y) {
        assert y >= x : "<randInt>: y is less than x";
        return rand.nextInt(Integer.MAX_VALUE - x) % (y - x + 1) + x;
    }

    public static double randFloat() {
        return rand.nextDouble();
    }

    public static double randInRange(double x, double y) {
        return x + randFloat() * (y - x);
    }

    public static boolean randBool() {
        if (randFloat() > 0.5) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return a random double in the range -1 < n < 1
     */
    public static double randomClamped() {
        return randFloat() - randFloat();
    }

    /**
     *
     * @return a random number with a normal distribution. See method at http://www.taygeta.com/random/gaussian.html
     */
    public static double randGaussian() {
        return randGaussian(0, 1);
    }

    public static double randGaussian(double mean, double standard_deviation) {

        double x1, x2, w, y1;

        if (use_last) /* use value from previous call */ {
            y1 = y2;
            use_last = false;
        } else {
            do {
                x1 = 2.0 * randFloat() - 1.0;
                x2 = 2.0 * randFloat() - 1.0;
                w = x1 * x1 + x2 * x2;
            } while (w >= 1.0);

            w = Math.sqrt((-2.0 * Math.log(w)) / w);
            y1 = x1 * w;
            y2 = x2 * w;
            use_last = true;
        }

        return (mean + y1 * standard_deviation);
    }

    //-----------------------------------------------------------------------
    //  some handy little functions
    //-----------------------------------------------------------------------
    public static double Sigmoid(double input) {
        return Sigmoid(input, 1.0);
    }

    public static double Sigmoid(double input, double response) {
        return (1.0 / (1.0 + Math.exp(-input / response)));
    }

//returns the maximum of two values
    public static <T extends Comparable> T MaxOf(T a, T b) {
        if (a.compareTo(b) > 0) {
            return a;
        }
        return b;
    }

//returns the minimum of two values
    public static <T extends Comparable> T MinOf(T a, T b) {
        if (a.compareTo(b) < 0) {
            return a;
        }
        return b;
    }

    /** 
     * clamps the first argument between the second two
     */
    public static <T extends Number> T clamp(final T arg, final T minVal, final T maxVal) {
        assert (minVal.doubleValue() < maxVal.doubleValue()) : "<Clamp>MaxVal < MinVal!";

        if (arg.doubleValue() < minVal.doubleValue()) {
            return  minVal;
        }
        if (arg.doubleValue() > maxVal.doubleValue()) {
            return maxVal;
        }
        return arg;
    }
}
