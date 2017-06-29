package Backend;

public class Position {
    public static final int HARD_CARRY = 0;
    public static final int MID_LANER = 1;
    public static final int OFF_LANER = 2;
    public static final int FARMING_SUPPORT = 3;
    public static final int HARD_SUPPORT = 4;

    public static double winRateAddend(char rating) {
        switch (rating) {
            case 'a': return -0.000; // meta
            case 'b': return -0.005; // situational
            case 'c': return -0.015; // niche
            case 'd': return -0.040; // cheese
            case 'e': return -0.090; // unfit
        }
        throw new IllegalArgumentException("invalid position rating: " + rating);
    }
}
