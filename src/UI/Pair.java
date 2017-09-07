package UI;

public class Pair<A, B> {
    public A a;
    public B b;

    Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Pair && a.equals(((Pair) o).a) && b.equals(((Pair) o).b);
    }
}
