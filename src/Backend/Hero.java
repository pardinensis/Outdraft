package Backend;

import java.util.ArrayList;

public class Hero {
    private int id;
    private String name;
    private String internalName;
    private String positionsStr;
    private double[] positionWRAddend;

    private double winRate;
    private double popularity;

    private ArrayList<Long> matchupsWon;
    private ArrayList<Long> matchupsPlayed;
    private double matchupsTotalWinRate;
    private ArrayList<Double> matchups;

    private ArrayList<Long> synergiesWon;
    private ArrayList<Long> synergiesPlayed;
    private double synergiesTotalWinRate;
    private ArrayList<Double> synergies;

    static int maxLength = 0;
    static String longestName;

    public Hero(int id, String name, String internalName) {
        this.id = id;
        this.name = name;
        this.internalName = internalName;

        matchupsWon = new ArrayList<>();
        matchupsPlayed = new ArrayList<>();
        matchupsTotalWinRate = 0;
        matchups = new ArrayList<>();

        synergiesWon = new ArrayList<>();
        synergiesPlayed = new ArrayList<>();
        synergiesTotalWinRate = 0;
        synergies = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setPositionsString(String positionsStr) {
        assert positionsStr.length() == 5;
        this.positionsStr = positionsStr;
        positionWRAddend = new double[5];
        for (int i = 0; i < 5; ++i) {
            positionWRAddend[i] = Position.winRateAddend(positionsStr.charAt(i));
        }
    }

    public String getPostionsString() {
        return positionsStr;
    }

    public double getPositionAddend(int pos) {
        return positionWRAddend[pos];
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public double getPopularity() {
        return popularity;
    }

    public double getPopularityWinRateAddend() {
        return -0.05 * Math.exp(-17 * popularity);
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public void setMatchup(int opponentId, long won, long played) {
        while (opponentId >= matchupsWon.size()) {
            matchupsWon.add(0L);
            matchupsPlayed.add(0L);
        }
        matchupsWon.set(opponentId, won);
        matchupsPlayed.set(opponentId, played);
    }

    public long getMatchupsWon(int opponentId) {
        if (opponentId >= matchupsWon.size()) {
            return 0L;
        }
        return matchupsWon.get(opponentId);
    }

    public long getMatchupsPlayed(int opponentId) {
        if (opponentId >= matchupsPlayed.size()) {
            return 0L;
        }
        return matchupsPlayed.get(opponentId);
    }

    public void setSynergy(int teammateId, long won, long played) {
        while (teammateId >= synergiesWon.size()) {
            synergiesWon.add(0L);
            synergiesPlayed.add(0L);
        }
        synergiesWon.set(teammateId, won);
        synergiesPlayed.set(teammateId, played);
    }

    public double getMatchupsTotalWinRate() {
        return matchupsTotalWinRate;
    }

    public double getSynergiesTotalWinRate() {
        return synergiesTotalWinRate;
    }

    public double getWinRateWith(int teammateId) {
        double myWR = winRate;
        double tmWR = Heroes.getInstance().getHero(teammateId).getWinRate();
        double expectedWR = (myWR * tmWR) / (myWR * tmWR + (1 - myWR) * (1 - tmWR));
//        System.out.println("Synergy " + name + " " + Heroes.getHero(teammateId).getName() + ": " + synergies.get(teammateId));
        return expectedWR + synergies.get(teammateId);
    }

    public double getWinRateAgainst(int opponentId) {
        double myWR = winRate;
        double oppWR = Heroes.getInstance().getHero(opponentId).getWinRate();
        double expectedWR = (myWR * (1 - oppWR)) / (myWR * (1 - oppWR) + oppWR * (1 - myWR));
//        System.out.println("Matchup " + name + " " + Heroes.getHero(opponentId).getName() + ": " + matchups.get(opponentId));
        return expectedWR + matchups.get(opponentId);
    }

    public void calculateTotalWinRates() {
        long totalMatchupsWon = 0L;
        long totalMatchupsPlayed = 0L;
        for (int i = 0; i < matchupsWon.size(); ++i) {
            totalMatchupsWon += matchupsWon.get(i);
            totalMatchupsPlayed += matchupsPlayed.get(i);
        }
        matchupsTotalWinRate = totalMatchupsWon / (double) totalMatchupsPlayed;

        long totalSynergiesWon = 0L;
        long totalSynergiesPlayed = 0L;
        for (int i = 0; i < synergiesWon.size(); ++i) {
            totalSynergiesWon += synergiesWon.get(i);
            totalSynergiesPlayed += synergiesPlayed.get(i);
        }
        synergiesTotalWinRate = totalSynergiesWon / (double) totalSynergiesPlayed;
    }

    public void analyzeMatchups() {
        for (int opponentId = 0; opponentId < matchupsWon.size(); ++opponentId) {
            if (opponentId == id) continue;
            Hero opponent = Heroes.getInstance().getHero(opponentId);
            if (opponent == null) continue;

            double myWR = matchupsTotalWinRate;
            double oppWR = opponent.getMatchupsTotalWinRate();
            double expectedWR = (myWR * (1 - oppWR)) / (myWR * (1 - oppWR) + oppWR * (1 - myWR));
            double measuredWR = matchupsWon.get(opponentId) / (double) matchupsPlayed.get(opponentId);

            while (opponentId >= matchups.size()) {
                matchups.add(0.0);
            }
            matchups.set(opponentId, measuredWR - expectedWR);
        }
    }

    public void analyzeSynergies() {
        for (int teammateId = 0; teammateId < synergiesWon.size(); ++teammateId) {
            if (teammateId == id) continue;
            Hero teammate = Heroes.getInstance().getHero(teammateId);
            if (teammate == null) continue;

            double myWR = synergiesTotalWinRate;
            double tmWR = teammate.getSynergiesTotalWinRate();
            double expectedWR = (myWR * tmWR) / (myWR * tmWR + (1 - myWR) * (1 - tmWR));
            double measuredWR = synergiesWon.get(teammateId) / (double) synergiesPlayed.get(teammateId);

            while (teammateId >= synergies.size()) {
                synergies.add(0.0);
            }
            synergies.set(teammateId, measuredWR - expectedWR);
        }
    }

    public String getBestSynergyStr() {
        int ally = -1;
        double bestSynergy = Double.NEGATIVE_INFINITY;
        for (int teammateId = 0; teammateId < synergies.size(); ++teammateId) {
            double syn = synergies.get(teammateId);
            if (syn > bestSynergy) {
                bestSynergy = syn;
                ally = teammateId;
            }
        }

        return name + " + " + Heroes.getInstance().getHero(ally).getName() + ": " + bestSynergy;
    }

    public double getSynergy(Hero other) {
        if (other.getId() == id) {
            return 0;
        }
        return synergies.get(other.getId());
    }

    public double getMatchup(Hero other) {
        if (other.getId() == id) {
            return 0;
        }
        return matchups.get(other.getId());
    }

    @Override
    public String toString() {
        return name;
    }
}
