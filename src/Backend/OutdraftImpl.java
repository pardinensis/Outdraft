package Backend;

import java.util.*;

public class OutdraftImpl implements Outdraft {
    private Draft draft;
    private Team team;

    private LinkedList<Hero> availableHeroes;

    private HashMap<String, PossiblePick> pickCache;
    private HashMap<String, PossiblePick> banCache;

    private LinkedList<Runnable> undoMoves;

    private Player[] playerAssignments;
    private LinkedList<Integer> playerAssignmentOrder;
    private int[] positionAssignments;

    public OutdraftImpl() {
    }

    public boolean init() {
        if (!Heroes.getInstance().initHeroes()) {
            return false;
        }

        pickCache = new HashMap<>();
        banCache = new HashMap<>();

        undoMoves = new LinkedList<>();

        playerAssignments = new Player[5];
        playerAssignmentOrder = new LinkedList<Integer>();
        positionAssignments = new int[] { -1, -1, -1, -1, -1 };

        draft = new Draft();

        return true;
    }

    @Override
    public void setPlayerAssignment(int pickId, Player player) {
        if (player != null) {
            if (player == Player.RANDOM_PLAYER) {
                int nRandoms = 5 - team.getActivePlayers().size();
                int nRandomsSet = 0;
                for (int j = 0; j < 5; ++j) {
                    if (playerAssignments[j] == Player.RANDOM_PLAYER) {
                        ++nRandomsSet;
                    }
                }
                if (nRandomsSet >= nRandoms) {
                    Iterator<Integer> it = playerAssignmentOrder.descendingIterator();
                    while (it.hasNext()) {
                        int pickIdIt = it.next();
                        if (playerAssignments[pickIdIt] == Player.RANDOM_PLAYER) {
                            playerAssignments[pickIdIt] = null;
                            break;
                        }
                    }
                }
            }
            else {
                for (int i = 0; i < 5; ++i) {
                    if (playerAssignments[i] == player) {
                        playerAssignments[i] = null;
                        break;
                    }
                }
            }
        }

        playerAssignments[pickId] = player;
        for (int i = 0; i < playerAssignmentOrder.size(); ++i) {
            if (playerAssignmentOrder.get(i) == pickId) {
                playerAssignmentOrder.remove(i);
                break;
            }
        }
        if (playerAssignmentOrder.size() > 4) {
            playerAssignmentOrder.remove(4);
        }
        playerAssignmentOrder.add(0, pickId);

        System.out.println(playerAssignmentOrder);
    }

    @Override
    public Player[] getPlayerAssignments() {
        return playerAssignments;
    }

    @Override
    public void setPositionAssignment(int pickId, int position) {
        if (position != -1) {
            for (int i = 0; i < 5; ++i) {
                if (positionAssignments[i] == position) {
                    positionAssignments[i] = -1;
                }
            }
        }
        positionAssignments[pickId] = position;
    }

    @Override
    public int[] getPositionAssignments() {
        return positionAssignments;
    }

    @Override
    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public Team getTeam() { return team; }

    @Override
    public synchronized void restart() {
        availableHeroes = Heroes.getInstance().getAvailableHeroes();
        draft.reset();
        clearCache();

        playerAssignments = new Player[5];
        positionAssignments = new int[] { -1, -1, -1, -1, -1 };

        undoMoves.clear();
    }

    @Override
    public synchronized void pickParty(String heroName) {
        clearCache();
        Hero hero = Heroes.getInstance().getHeroByName(heroName);
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
        Hero hero = Heroes.getInstance().getHeroByName(heroName);
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
        Hero hero = Heroes.getInstance().getHeroByName(heroName);
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
        ArrayList<Player> players = team.getActivePlayers();
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

    public ArrayList<PickAssignment> filterPickAssignments(ArrayList<PickAssignment> pickAssignments, ArrayList<Hero> heroes) {
        ArrayList<PickAssignment> filteredAssignments = new ArrayList<>();

        mainLoop:
        for (PickAssignment assignment : pickAssignments) {
            Hero[] heroPositions = assignment.getHeroes();
            Player[] playerPositions = assignment.getPlayers();
            for (int pos = 0; pos < 5; ++pos) {
                Hero hero = heroPositions[pos];
                Player player = playerPositions[pos];
                for (int pickPos = 0; pickPos < heroes.size(); ++pickPos) {
                    Hero pickedHero = heroes.get(pickPos);
                    if (hero != null && pickedHero == hero) {
                        Player assignedPlayer = playerAssignments[pickPos];
                        if (assignedPlayer != null) {
                            if (assignedPlayer == Player.RANDOM_PLAYER) {
                                if (player != null) {
                                    continue mainLoop;
                                }
                            }
                            else if(assignedPlayer != player) {
                                continue mainLoop;
                            }
                        }
                        int assignedPosition = positionAssignments[pickPos];
                        if (assignedPosition != -1 && assignedPosition != pos) {
                            continue mainLoop;
                        }
                    }
                }
            }

            filteredAssignments.add(assignment);
        }

//        System.out.println(pickAssignments.size() + " -> " + filteredAssignments.size());

        return filteredAssignments;
    }

    public PickAssignment chooseBestPickAssignment(ArrayList<Hero> heroes, Team team) {
        ArrayList<PickAssignment> pickAssignments = generatePickAssignments(heroes, team);

        if (team.getActivePlayers().size() > 0) {
            pickAssignments = filterPickAssignments(pickAssignments, heroes);
        }

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
            Hero hero = Heroes.getInstance().getHeroByName(heroName);
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
            Hero hero = Heroes.getInstance().getHeroByName(heroName);
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
        return availableHeroes.contains(Heroes.getInstance().getHeroByName(heroName));
    }

    @Override
    public synchronized void clearCache() {
        pickCache.clear();
        banCache.clear();
    }
}
