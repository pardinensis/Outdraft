package Backend;

public class PossiblePick {
    private Hero hero;
    private PickAssignment ownPickAssignment, enemyPickAssignment;
    private double winRate;

    public PossiblePick(Hero hero, PickAssignment ownPickAssignment, PickAssignment enemyPickAssignment, double winRate) {
        this.hero = hero;
        this.ownPickAssignment = ownPickAssignment;
        this.enemyPickAssignment = enemyPickAssignment;
        this.winRate = winRate;
    }

    public Hero getHero() {
        return hero;
    }

    public PickAssignment getOwnPickAssignment() {
        return ownPickAssignment;
    }

    public PickAssignment getEnemyPickAssignment() {
        return enemyPickAssignment;
    }

    public double getWinRate() {
        double product = winRate;
        double invProduct = 1 - winRate;

        product *= ownPickAssignment.getRating();
        invProduct *= 1 - ownPickAssignment.getRating();

        product *= 1 - enemyPickAssignment.getRating();
        invProduct *= enemyPickAssignment.getRating();

        return product / (product + invProduct);
    }
}
