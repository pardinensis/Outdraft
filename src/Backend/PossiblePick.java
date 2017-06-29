package Backend;

public class PossiblePick {
    private Hero hero;
    private PickAssignment pickAssignment;
    private double winRate;
    private double advantage;

    public PossiblePick(Hero hero, PickAssignment pickAssignment, double winRate) {
        this.hero = hero;
        this.pickAssignment = pickAssignment;
        this.winRate = winRate;
    }

    public PossiblePick(PossiblePick other) {
        this.hero = other.getHero();
        this.pickAssignment = other.getPickAssignment();
        this.winRate = other.getWinRate();
    }

    public Hero getHero() {
        return hero;
    }

    public PickAssignment getPickAssignment() {
        return pickAssignment;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public double getAdvantage() {
        return advantage;
    }

    public void setAdvantage(double advantage) {
        this.advantage = advantage;
    }

    public double rate() {
        if (pickAssignment != null) {
            double paRating = pickAssignment.getRating();
            return winRate * paRating / (winRate * paRating + (1 - winRate) * (1 - paRating));
        }
        return winRate;
    }
}
