package Backend;

import java.util.ArrayList;

public class Draft {
    private ArrayList<Hero> ownHeroes;
    private ArrayList<Hero> enemyHeroes;

    public Draft() {
        this.ownHeroes = new ArrayList<>();
        this.enemyHeroes = new ArrayList<>();
    }

    public Draft(Draft other) {
        this.ownHeroes = new ArrayList<>(other.ownHeroes);
        this.enemyHeroes = new ArrayList<>(other.enemyHeroes);
    }

    public void reset() {
        ownHeroes.clear();
        enemyHeroes.clear();
    }

    public void addOwnHero(Hero hero) {
        ownHeroes.add(hero);
    }

    public void addEnemyHero(Hero hero) {
        enemyHeroes.add(hero);
    }

    public void removeOwnHero(Hero hero) {
        ownHeroes.remove(hero);
    }

    public void removeEnemyHero(Hero hero) {
        enemyHeroes.remove(hero);
    }

    public ArrayList<Hero> getOwnHeroes() {
        return ownHeroes;
    }

    public ArrayList<Hero> getEnemyHeroes() {
        return enemyHeroes;
    }

    public double calculateIndependentWinRate() {
        double winRateProduct = 1;
        double winRateProductInv = 1;

        for (Hero j : ownHeroes) {
            double winRate = j.getWinRate();
            winRateProduct *= winRate;
            winRateProductInv *= 1 - winRate;
        }

        for (Hero j : enemyHeroes) {
            double winRate = j.getWinRate();
            winRateProduct *= 1 - winRate;
            winRateProductInv *= winRate;
        }

        return winRateProduct / (winRateProduct + winRateProductInv);
    }

    public double calculateExpectedWinRate() {
        double winRateProduct = 1;
        double winRateProductInv = 1;

        // rate own synergy
        for (Hero j : ownHeroes) {
            for (Hero k : ownHeroes) {
                if (j.getId() < k.getId()) {
                    double winRate = j.getWinRateWith(k.getId());
                    winRateProduct *= winRate;
                    winRateProductInv *= 1 - winRate;
                }
            }
        }

        // rate enemy synergy
        for (Hero j : enemyHeroes) {
            for (Hero k : enemyHeroes) {
                if (j.getId() < k.getId()) {
                    double winRate = j.getWinRateWith(k.getId());
                    winRateProduct *= 1 - winRate;
                    winRateProductInv *= winRate;
                }
            }
        }

        // rate matchups
        for (Hero j : ownHeroes) {
            for (Hero k : enemyHeroes) {
                double winRate = j.getWinRateAgainst(k.getId());
                winRateProduct *= winRate;
                winRateProductInv *= 1 - winRate;
            }
        }

        final double pubWinrateFactor = 1;

        // normalize hero win rates
        int n = ownHeroes.size() + enemyHeroes.size();
        for (Hero h : ownHeroes) {
            winRateProduct *= Math.pow(h.getWinRate(), 1 + pubWinrateFactor - n);
            winRateProductInv *= Math.pow(1 - h.getWinRate(), 1 + pubWinrateFactor - n);
        }
        for (Hero h : enemyHeroes) {
            winRateProduct *= Math.pow(1 - h.getWinRate(), 1 + pubWinrateFactor - n);
            winRateProductInv *= Math.pow(h.getWinRate(), 1 + pubWinrateFactor - n);
        }

        return winRateProduct / (winRateProduct + winRateProductInv);
    }
}
