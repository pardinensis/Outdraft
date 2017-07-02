package Backend;

import java.util.*;

public class OutdraftImpl implements Outdraft {
    private Draft draft;
    private Team team;

    private LinkedList<Hero> availableHeroes;

    private HashMap<String, PossiblePick> pickCache;
    private HashMap<String, PossiblePick> banCache;

    private LinkedList<Runnable> undoMoves;

    public OutdraftImpl() {
    }

    public boolean init() {
        if (!Heroes.initHeroes()) {
            return false;
        }

        pickCache = new HashMap<>();
        banCache = new HashMap<>();

        undoMoves = new LinkedList<>();

        draft = new Draft();

        return true;
    }


    @Override
    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public synchronized void restart() {
        availableHeroes = Heroes.getAvailableHeroes();
        draft.reset();
        clearCache();

        undoMoves.clear();
    }

    @Override
    public synchronized void pickParty(String heroName) {
        clearCache();
        Hero hero = Heroes.getHeroByName(heroName);
        draft.addOwnHero(hero);
        availableHeroes.remove(hero);

        undoMoves.add(() -> {
           draft.removeOwnHero(hero);
           availableHeroes.add(hero);
        });
    }

    @Override
    public synchronized void pickEnemy(String heroName) {
        clearCache();
        Hero hero = Heroes.getHeroByName(heroName);
        draft.addEnemyHero(hero);
        availableHeroes.remove(hero);

        undoMoves.add(() -> {
           draft.removeEnemyHero(hero);
           availableHeroes.add(hero);
        });
    }

    @Override
    public synchronized void ban(String heroName) {
        clearCache();
        Hero hero = Heroes.getHeroByName(heroName);
        availableHeroes.remove(hero);

        undoMoves.add(() -> {
            availableHeroes.add(hero);
        });
    }

    @Override
    public synchronized void undo() {
        if (undoMoves.size() > 0) {
            clearCache();
            undoMoves.removeLast().run();
        }
    }

    public ArrayList<String> generatePermutations(int k) {
        if (k > 5) {
            return null;
        }

        ArrayList<String> permutations = new ArrayList<>();
        if (k == 0) {
            permutations.add("");
            return permutations;
        }

        ArrayList<String> sublist = generatePermutations(k - 1);
        for (String str : sublist) {
            for (char c = '0'; c < '5'; ++c) {
                if (str.indexOf(c) == -1) {
                    permutations.add(str + c);
                }
            }
        }
        return permutations;
    }


    public ArrayList<PickAssignment> generatePickAssignments(ArrayList<Hero> heroes, Team team) {
        ArrayList<Player> players = team.getPlayers();
        final int nHeroes = heroes.size();
        final int nPlayers = players.size();
        ArrayList<String> heroPermutations = generatePermutations(nHeroes);
        ArrayList<String> playerPermutations = generatePermutations(nPlayers);

        ArrayList<PickAssignment> pickAssignments = new ArrayList<>();
        if (heroPermutations != null) {
            for (String heroPermutation : heroPermutations) {
                Hero[] heroPositions = new Hero[5];
                for (int i = 0; i < nHeroes; ++i) {
                    heroPositions[heroPermutation.charAt(i) - '0'] = heroes.get(i);
                }
                for (String playerPermutation : playerPermutations) {
                    Player[] playerPositions = new Player[5];
                    for (int i = 0; i < nPlayers; ++i) {
                        playerPositions[playerPermutation.charAt(i) - '0'] = players.get(i);
                    }
                    PickAssignment pa = new PickAssignment(heroPositions, playerPositions);
                    pickAssignments.add(pa);
                }
            }
        }
        return pickAssignments;
    }

    public PickAssignment chooseBestPickAssignment(ArrayList<Hero> heroes, Team team) {
        ArrayList<PickAssignment> pickAssignments = generatePickAssignments(heroes, team);
        double maxRating = Double.NEGATIVE_INFINITY;
        PickAssignment bestPickAssignment = null;
        for (PickAssignment pa : pickAssignments) {
            double rating = pa.getRating();
            if (rating > maxRating) {
                maxRating = rating;
                bestPickAssignment = pa;
            }
        }
        return bestPickAssignment;
    }

    @Override
    public ArrayList<PossiblePick> ratePicks() {
        ArrayList<PossiblePick> possiblePicks = new ArrayList<>();

        for (Hero hero : availableHeroes) {
            PossiblePick possiblePick = getPossiblePick(hero.getName());
            if (possiblePick != null && possiblePick.getOwnPickAssignment() != null) {
                possiblePicks.add(possiblePick);
            }
        }

        possiblePicks.sort((PossiblePick a, PossiblePick b) -> new Double(b.getWinRate()).compareTo(a.getWinRate()));
        return possiblePicks;
    }

    @Override
    public ArrayList<PossiblePick> rateBans() {
        ArrayList<PossiblePick> possibleBans = new ArrayList<>();

        for (Hero hero : availableHeroes) {
            PossiblePick possibleBan = getPossibleBan(hero.getName());
            if (possibleBan != null && possibleBan.getOwnPickAssignment() != null) {
                possibleBans.add(possibleBan);
            }
        }

        possibleBans.sort((PossiblePick a, PossiblePick b) -> new Double(b.getWinRate()).compareTo(a.getWinRate()));
        return possibleBans;
    }

    @Override
    public synchronized PossiblePick getPossiblePick(String heroName) {
        PossiblePick possiblePick = pickCache.get(heroName);

        if (possiblePick == null) {
            Hero hero = Heroes.getHeroByName(heroName);
            Draft d = new Draft(draft);
            d.addOwnHero(hero);
            double winRate = d.calculateExpectedWinRate();

            PickAssignment ownPickAssignment = chooseBestPickAssignment(d.getOwnHeroes(), team);
            if (ownPickAssignment == null)
                return null;

            PickAssignment enemyPickAssignment = chooseBestPickAssignment(d.getEnemyHeroes(), new Team());
            if (enemyPickAssignment == null)
                return null;

            possiblePick = new PossiblePick(hero, ownPickAssignment, enemyPickAssignment, winRate);
            pickCache.put(heroName, possiblePick);
        }

        return possiblePick;
    }

    public PossiblePick getPossibleBan(String heroName) {
        PossiblePick possibleBan = banCache.get(heroName);

        if (possibleBan == null) {
            Hero hero = Heroes.getHeroByName(heroName);
            Draft d = new Draft(draft);
            d.addEnemyHero(hero);
            double winRate = 1 - d.calculateExpectedWinRate();
            PickAssignment ownPickAssignment = chooseBestPickAssignment(d.getOwnHeroes(), team);
            PickAssignment enemyPickAssignment = chooseBestPickAssignment(d.getEnemyHeroes(), new Team());
            possibleBan = new PossiblePick(hero, enemyPickAssignment, ownPickAssignment, winRate);
            banCache.put(heroName, possibleBan);
        }


        return possibleBan;
    }

    public PossiblePick getCurrentState() {
        PickAssignment allyPickAssignment = chooseBestPickAssignment(draft.getOwnHeroes(), team);
        PickAssignment enemyPickAssignment = chooseBestPickAssignment(draft.getEnemyHeroes(), new Team());
        return new PossiblePick(null, allyPickAssignment, enemyPickAssignment, draft.calculateExpectedWinRate());
    }

    public boolean isAvailable(String heroName) {
        return availableHeroes.contains(Heroes.getHeroByName(heroName));
    }

    private synchronized void clearCache() {
        pickCache.clear();
        banCache.clear();
    }
}
