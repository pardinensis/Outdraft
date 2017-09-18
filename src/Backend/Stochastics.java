package Backend;

import java.util.ArrayList;

public class Stochastics {
    public static double combine(double... winRates) {
        double prod = 1;
        double invProd = 1;
        for (double wr : winRates) {
            prod *= wr;
            invProd *= 1 / wr;
        }
        return prod / (prod + invProd);
    }

    public static double combine(ArrayList<Double> winRates) {
        double prod = 1;
        double invProd = 1;
        for (double wr : winRates) {
            prod *= wr;
            invProd *= 1 / wr;
        }
        return prod / (prod + invProd);
    }
}
