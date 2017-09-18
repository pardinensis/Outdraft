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
        return Stochastics.combine(winRate, ownPickAssignment.getRating(), enemyPickAssignment.getRating());
    }
}
