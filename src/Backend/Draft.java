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
        ArrayList<Double> ratings = new ArrayList<>();

        for (Hero j : ownHeroes) {
            ratings.add(j.getWinRate());
        }

        for (Hero j : enemyHeroes) {
            ratings.add(j.getWinRate());
        }

        return Stochastics.combine(ratings);
    }

    public double calculateExpectedWinRate() {
        ArrayList<Double> ratings = new ArrayList<>();

        // rate own synergy
        for (Hero j : ownHeroes) {
            for (Hero k : ownHeroes) {
                if (j.getId() < k.getId()) {
                    ratings.add(j.getWinRateWith(k.getId()));
                }
            }
        }

        // rate enemy synergy
        for (Hero j : enemyHeroes) {
            for (Hero k : enemyHeroes) {
                if (j.getId() < k.getId()) {
                    ratings.add(j.getWinRateWith(k.getId()));
                }
            }
        }

        // rate matchups
        for (Hero j : ownHeroes) {
            for (Hero k : enemyHeroes) {
                ratings.add(j.getWinRateAgainst(k.getId()));
            }
        }

        double winRateProduct = Stochastics.combine(ratings);
        double winRateProductInv = 1 - winRateProduct;

        // normalize hero win rates
        int n = ownHeroes.size() + enemyHeroes.size();
        for (Hero h : ownHeroes) {
            winRateProduct *= Math.pow(h.getWinRate(), 1 + PickAssignment.HERO_WIN_RATE_FACTOR - n);
            winRateProductInv *= Math.pow(1 - h.getWinRate(), 1 + PickAssignment.HERO_WIN_RATE_FACTOR - n);
        }
        for (Hero h : enemyHeroes) {
            winRateProduct *= Math.pow(1 - h.getWinRate(), 1 + PickAssignment.HERO_WIN_RATE_FACTOR - n);
            winRateProductInv *= Math.pow(h.getWinRate(), 1 + PickAssignment.HERO_WIN_RATE_FACTOR - n);
        }

        return winRateProduct / (winRateProduct + winRateProductInv);
    }
}
