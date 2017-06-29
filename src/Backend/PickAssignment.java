package Backend;

import java.text.DecimalFormat;

public class PickAssignment {
    private Hero[] heroes;
    private Player[] players;
    private double rating;

    public PickAssignment(Hero[] heroes, Player[] players) {
        assert heroes.length == 5;
        assert players.length == 5;
        this.heroes = heroes;
        this.players = players;
        rating = rate();
    }

    private double rate() {
        double product = 1;
        double productInv = 1;
        for (int position = 0; position < 5; ++position) {
            boolean heroSet = heroes[position] != null;
            boolean playerSet = players[position] != null;

            if (heroSet) {
                double addend = heroes[position].getPositionAddend(position);
                product *= 0.5 + addend;
                productInv *= 0.5 - addend;
            }

            if (playerSet) {
                double addend = players[position].getPositionAddend(position);
                product *= 0.5 + addend;
                productInv *= 0.5 - addend;
            }

            if (heroSet && playerSet) {
                double addend = players[position].getWinRateAddend(heroes[position]);
                product *= 0.5 + addend;
                productInv *= 0.5 - addend;
            }

            if (heroSet && !playerSet) {
                double addend = heroes[position].getPopularityWinRateAddend();
                product *= 0.5 + addend;
                productInv *= 0.5 - addend;
            }
        }
        return product / (product + productInv);
    }

    public double getRating() {
        return rating;
    }

    public int getPositionFor(Hero hero) {
        for (int i = 0; i < 5; ++i) {
            if (heroes[i] == hero) {
                return i;
            }
        }
        return -1;
    }

    public Hero[] getHeroes() {
        return heroes;
    }

    public Player[] getPlayers() {
        return players;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.00");
        for (int i = 0; i < 5; ++i) {
            str.append(i + 1).append(": ").append(heroes[i]).append(" - ").append(players[i]);
            if (heroes[i] != null)
                str.append(" H=" + df.format(heroes[i].getPositionAddend(i) * 100));
            if (players[i] != null)
                str.append(" P=" + df.format(players[i].getPositionAddend(i) * 100));
            if (heroes[i] != null && players[i] != null)
                str.append(" PH=" + df.format(players[i].getWinRateAddend(heroes[i]) * 100));
            if (i < 4)
                str.append("\n");
        }
        return str.toString();
    }
}