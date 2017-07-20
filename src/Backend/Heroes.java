package Backend;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Heroes {
    private static ArrayList<Hero> heroes;
    private static HashMap<String, Integer> heroMap;

    private static boolean checkUpdateTimestamp() {
        boolean updateNecessary = false;
        long daysSinceEpoch = ChronoUnit.DAYS.between(LocalDate.ofEpochDay(0), LocalDate.now());
        try {
            long lastUpdate = new Scanner(new File(Resources.FILENAME_CACHE_UPDATE_TIMESTAMP)).nextLong();
            if (lastUpdate < daysSinceEpoch) {
                updateNecessary = true;
            }
        } catch (FileNotFoundException e) {
            updateNecessary = true;
        }
        return updateNecessary;
    }

    private static void writeUpdateTimestamp() {
        long daysSinceEpoch = ChronoUnit.DAYS.between(LocalDate.ofEpochDay(0), LocalDate.now());
        try {
            Writer writer = new FileWriter(new File(Resources.FILENAME_CACHE_UPDATE_TIMESTAMP));
            writer.write(String.valueOf(daysSinceEpoch));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateCache() {
        if (!checkUpdateTimestamp())
            return;

        heroes = new ArrayList<>();
        heroMap = new HashMap<>();

        boolean updateFailed = false;

        // load hero list
        if (initHeroListFromAPI())
            writeHeroListToCache();

        // load public win rates and popularity
        int mmrBracket = loadMMRBracket();
        if (loadWinRatesAndPopularityFromDotabuff(mmrBracket)) {
            writeWinRatesAndPopularityToCache();
        }
        else {
            updateFailed = true;
        }

        // load matchups
        if (loadMatchupsFromDotabuff()) {
            writeMatchupsToCache();
        }
        else {
            updateFailed = true;
        }

        if (!updateFailed) {
            writeUpdateTimestamp();
        }

        initHeroes();
    }

    public static boolean initHeroes() {
        heroes = new ArrayList<>();
        heroMap = new HashMap<>();

        // load hero list
        if (!initHeroListFromCache()) {
            return false;
        }

        // load positions
        loadPositions();

        // load public win rates and popularity
        loadWinRatesAndPopularityFromCache();

        // load matchups
        loadMatchupsFromCache();

        // load synergies
        loadSynergiesFromCache();

        // analyze matchups and synergies
        for (Hero hero : heroes) {
            if (hero != null) {
                hero.calculateTotalWinRates();
            }
        }
        for (Hero hero : heroes) {
            if (hero != null) {
                hero.analyzeMatchups();
                hero.analyzeSynergies();
            }
        }
//
//        try {
//            String filename = "synergies.txt";
//            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
//            for (Hero hero : heroes) {
//                if (hero == null) continue;
//                bw.write(hero.getBestSynergyStr());
//                bw.newLine();
//            }
//            bw.close();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }

        return true;
    }

    public static Hero getHero(int id) {
        return heroes.get(id);
    }

    public static Hero getHeroByName(String heroName) {
        return getHero(heroMap.get(heroName));
    }

    public static int getHeroesUpperBound() {
        return heroes.size();
    }

    private static boolean initHeroListFromAPI() {
        String query = Resources.API_GET_HEROES + "key=" + Resources.API_KEY + "&language=en_us";
        Map<String, Object> response = Resources.getObject(query);

        if (response == null)
            return false;

        Map<String, Object> result = (Map<String, Object>) response.get("result");
        List<Map<String, Object>> heroList = (List<Map<String, Object>>) result.get("heroes");
        for (Map<String, Object> heroEntry : heroList) {
            int id = ((Double) heroEntry.get("id")).intValue();
            String name = (String) heroEntry.get("localized_name");
            String internalName = (String) heroEntry.get("name");
            addHero(id, name, internalName);
        }

        return  true;
    }

    private static boolean initHeroListFromCache() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Resources.FILENAME_CACHE_HEROES));
            br.readLine(); // drop header

            String line;
            while ((line = br.readLine()) != null) {
                String[] token = line.split(",");
                assert token.length == 3;
                addHero(Integer.valueOf(token[0]), token[1], token[2]);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void writeHeroListToCache() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(Resources.FILENAME_CACHE_HEROES));
            bw.write("Id,Name,InternalName");
            bw.newLine();
            for (int id = 0; id < heroes.size(); ++id) {
                Hero hero = heroes.get(id);
                if (hero != null) {
                    bw.write(id + "," + hero.getName() + "," + hero.getInternalName());
                    bw.newLine();
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadPositions() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Resources.FILENAME_CONFIG_POSITIONS));
            br.readLine(); // drop header

            String line;
            while ((line = br.readLine()) != null) {
                String[] token = line.split(",");
                assert token.length == 2;
                heroes.get(Integer.valueOf(token[0])).setPositionsString(token[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int loadMMRBracket() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Resources.FILENAME_CONFIG_MMR_BRACKET));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals("") || line.startsWith("#")) continue;
                int skillBracket = Integer.valueOf(line);
                if (skillBracket < 1 || skillBracket > 5) {
                    System.err.println("Invalid MMR bracket number: " + skillBracket);
                } else {
                    return skillBracket;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private static boolean loadWinRatesAndPopularityFromDotabuff(int skillBracket) {
        try {
            Document doc = Jsoup.connect("https://www.dotabuff.com/heroes/meta").get();
            Elements rows = doc.select("tr");
            for (Element row : rows) {
                Elements children = row.children();
                if (children.size() == 12) {
                    int column = 1 + 2 * skillBracket;
                    String heroName = row.child(0).attr("data-value");
                    double winRate = new Double(row.child(column).attr("data-value"));
                    double popularity = new Double(row.child(column - 1).attr("data-value"));
                    Hero hero = heroes.get(heroMap.get(heroName));
                    hero.setWinRate(winRate * 0.01);
                    hero.setPopularity(popularity * 0.01);
                }
            }
            return true;
        } catch (UnknownHostException e) {
            // ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void loadWinRatesAndPopularityFromCache() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Resources.FILENAME_CACHE_WINRATES_POPULARITY));
            br.readLine(); // drop header

            String line;
            while ((line = br.readLine()) != null) {
                String[] token = line.split(",");
                assert token.length == 3;
                Hero hero = heroes.get(Integer.valueOf(token[0]));
                hero.setWinRate(Double.valueOf(token[1]));
                hero.setPopularity(Double.valueOf(token[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeWinRatesAndPopularityToCache() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(Resources.FILENAME_CACHE_WINRATES_POPULARITY));
            bw.write("Id,WinRate,Popularity");
            bw.newLine();
            DecimalFormat decimalFormat = new DecimalFormat("0.0######");
            for (int id = 0; id < heroes.size(); ++id) {
                Hero hero = heroes.get(id);
                if (hero != null) {
                    bw.write(id + "," + decimalFormat.format(hero.getWinRate()) + "," +
                            decimalFormat.format(hero.getPopularity()));
                    bw.newLine();
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean loadMatchupsFromDotabuff() {
        HashMap<String, String> heroPages = new HashMap<>();
        Document doc = null;
        try {
            doc = Jsoup.connect("https://www.dotabuff.com/heroes").get();
            Elements divs = doc.select("div[class=\"name\"]");
            for (Element div : divs) {
                String heroName = div.text();
                String heroPage = div.parent().parent().attr("abs:href");
                heroPages.put(heroName, heroPage);
            }

            for (Hero hero : heroes) {
                if (hero == null) continue;
                String matchupPage = heroPages.get(hero.getName()) + "/matchups?date=year";

                doc = Jsoup.connect(matchupPage).get();
                Elements rows = doc.select("tr");
                for (Element row : rows) {
                    Elements children = row.children();
                    if (children.size() == 5) {
                        String opponentStr = row.child(0).attr("data-value");
                        double winRate = new Double(row.child(3).attr("data-value"));
                        long matchesPlayed = new Integer(row.child(4).attr("data-value"));
                        long matchesWon = (long) (0.01 * winRate * matchesPlayed);
                        hero.setMatchup(heroMap.get(opponentStr), matchesWon, matchesPlayed);
                    }
                }
            }
            return true;
        } catch (IOException e) {
            System.out.println("Could not connect to Dotabuff");
//            e.printStackTrace();
        }
        return false;
    }

    private static void loadMatchupsFromCache() {
        try {
            for (Hero hero : heroes) {
                if (hero == null) continue;
                String filename = Resources.DIRNAME_CACHE_MATCHUPS + hero.getId() + ".txt";
                BufferedReader br = new BufferedReader(new FileReader(filename));
                String line;
                int id = 0;
                while ((line = br.readLine()) != null) {
                    String[] token = line.split("/");
                    assert token.length == 2;
                    hero.setMatchup(id++, Long.valueOf(token[0]), Long.valueOf(token[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadSynergiesFromCache() {
        try {
            for (Hero hero : heroes) {
                if (hero == null) continue;
                String filename = Resources.DIRNAME_CACHE_SYNERGIES + hero.getId() + ".txt";
                BufferedReader br = new BufferedReader(new FileReader(filename));
                String line;
                int id = 0;
                while ((line = br.readLine()) != null) {
                    String[] token = line.split("/");
                    assert token.length == 2;
                    hero.setSynergy(id++, Long.valueOf(token[0]), Long.valueOf(token[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeMatchupsToCache() {
        try {
            for (Hero hero : heroes) {
                if (hero == null) continue;
                String filename = Resources.DIRNAME_CACHE_MATCHUPS + hero.getId() + ".txt";
                BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
                for (Hero opp : heroes) {
                    long matchupsWon = 0L;
                    long matchupsPlayed = 0L;
                    if (opp != null) {
                        matchupsWon = hero.getMatchupsWon(opp.getId());
                        matchupsPlayed = hero.getMatchupsPlayed(opp.getId());
                    }
                    bw.write(matchupsWon + "/" + matchupsPlayed);
                    bw.newLine();
                }
                bw.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addHero(int id, String name, String internalName) {
        Hero hero = new Hero(id, name, internalName);
        while (heroes.size() <= id) {
            heroes.add(null);
        }
        heroes.set(id, hero);
        heroMap.put(name, id);
    }

    public static LinkedList<Hero> getAvailableHeroes() {
        ArrayList<Hero> bannedHeroes = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(Resources.FILENAME_CONFIG_EXCLUDED_HEROES));
            String line;
            while ((line = br.readLine()) != null) {
                Hero hero = getHeroByName(line);
                bannedHeroes.add(hero);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LinkedList<Hero> availableHeroes = new LinkedList<>();
        for (Hero hero : heroes) {
            if (hero != null && !bannedHeroes.contains(hero)) {
                availableHeroes.add(hero);
            }
        }
        return availableHeroes;
    }
}
