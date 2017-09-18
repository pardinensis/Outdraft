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
import java.util.function.Consumer;

public class Heroes {
    private static final int N_SKILL_BRACKETS = 5;

    private ArrayList<Hero> heroes;
    private HashMap<String, Integer> heroMap;

    private static Heroes heroesInstance = null;
    private static volatile Heroes updatedInstance = null;

    private int mmrBracket;


    public static Heroes getInstance() {
        if (heroesInstance == null) {
            heroesInstance = new Heroes();
        }
        return heroesInstance;
    }

    public static void replaceInstanceIfUpdated() {
        synchronized (Heroes.class) {
            if (updatedInstance != null) {
                heroesInstance = updatedInstance;
                updatedInstance = null;
            }
        }
    }

    private boolean checkUpdateTimestamp() {
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

    private void writeUpdateTimestamp() {
        long daysSinceEpoch = ChronoUnit.DAYS.between(LocalDate.ofEpochDay(0), LocalDate.now());
        try {
            Writer writer = new FileWriter(new File(Resources.FILENAME_CACHE_UPDATE_TIMESTAMP));
            writer.write(String.valueOf(daysSinceEpoch));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateCache(Consumer<String> statusOutputFunction) {
        Heroes heroes = new Heroes();
        if (!heroes.checkUpdateTimestamp()) {
            statusOutputFunction.accept("already up to date");
            return;
        }

        heroes.heroes = new ArrayList<>();
        heroes.heroMap = new HashMap<>();

        boolean updateFailed = false;

        // load hero list
        statusOutputFunction.accept("updating hero list");
        if (heroes.initHeroListFromAPI())
            heroes.writeHeroListToCache();

        // load public win rates and popularity
        statusOutputFunction.accept("updating public win rates");
        if (heroes.loadWinRatesAndPopularityFromDotabuff()) {
            heroes.writeWinRatesAndPopularityToCache();
        }
        else {
            updateFailed = true;
        }

        // load matchups
        statusOutputFunction.accept("updating matchups");
        if (heroes.loadMatchupsFromDotabuff()) {
            heroes.writeMatchupsToCache();
        }
        else {
            updateFailed = true;
        }

        if (!updateFailed) {
            heroes.writeUpdateTimestamp();
        }

        heroes.initHeroes();
        synchronized (Heroes.class) {
            updatedInstance = heroes;
        }

        statusOutputFunction.accept((updateFailed) ? "update failed" : "update finished");
    }

    public boolean initHeroes() {
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

        setMMRBracket(loadMMRBracket());

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

    public Hero getHero(int id) {
        return heroes.get(id);
    }

    public Hero getHeroByName(String heroName) {
        return getHero(heroMap.get(heroName));
    }

    public int getHeroesUpperBound() {
        return heroes.size();
    }

    private boolean initHeroListFromAPI() {
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

    private boolean initHeroListFromCache() {
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

    private void writeHeroListToCache() {
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

    private void loadPositions() {
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

    public void writePositions() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(Resources.FILENAME_CONFIG_POSITIONS));
            bw.write("Id,Positions");
            bw.newLine();
            for (int id = 0; id < heroes.size(); ++id) {
                Hero hero = heroes.get(id);
                if (hero != null) {
                    bw.write(id + "," + hero.getPostionsString());
                    bw.newLine();
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int loadMMRBracket() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Resources.FILENAME_CONFIG_MMR_BRACKET));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals("") || line.startsWith("#")) continue;
                int skillBracket = Integer.valueOf(line);
                if (skillBracket < 0 || skillBracket >= N_SKILL_BRACKETS) {
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

    private boolean loadWinRatesAndPopularityFromDotabuff() {
        try {
            Document doc = Jsoup.connect("https://www.dotabuff.com/heroes/meta").get();
            Elements rows = doc.select("tr");
            for (Element row : rows) {
                Elements children = row.children();
                if (children.size() == 12) {
                    String heroName = row.child(0).attr("data-value");
                    Hero hero = heroes.get(heroMap.get(heroName));
                    for (int skillBracket = 0; skillBracket < N_SKILL_BRACKETS; ++skillBracket) {
                        int column = 3 + 2 * skillBracket;
                        double winRate = new Double(row.child(column).attr("data-value"));
                        double popularity = new Double(row.child(column - 1).attr("data-value"));
                        hero.setWinRateBracket(skillBracket, winRate * 0.01);
                        hero.setPopularityBracket(skillBracket, popularity * 0.01);
                    }
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

    private void loadWinRatesAndPopularityFromCache() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Resources.FILENAME_CACHE_WINRATES_POPULARITY));
            br.readLine(); // drop header

            String line;
            while ((line = br.readLine()) != null) {
                String[] token = line.split(",");
                assert token.length == 1 + 2 * N_SKILL_BRACKETS;
                Hero hero = heroes.get(Integer.valueOf(token[0]));
                for (int skillBracket = 0; skillBracket < N_SKILL_BRACKETS; ++skillBracket) {
                    hero.setWinRateBracket(skillBracket, Double.valueOf(token[1 + 2 * skillBracket]));
                    hero.setPopularityBracket(skillBracket, Double.valueOf(token[2 + 2 * skillBracket]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeWinRatesAndPopularityToCache() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(Resources.FILENAME_CACHE_WINRATES_POPULARITY));
            bw.write("Id,WinRate,Popularity");
            bw.newLine();
            DecimalFormat decimalFormat = new DecimalFormat("0.0######");
            for (int id = 0; id < heroes.size(); ++id) {
                Hero hero = heroes.get(id);
                if (hero != null) {
                    StringBuilder str = new StringBuilder("" + id);
                    for (int skillBracket = 0; skillBracket < N_SKILL_BRACKETS; ++skillBracket) {
                        str.append(",").append(decimalFormat.format(hero.getWinRateBracket(skillBracket))).append(",")
                                .append(decimalFormat.format(hero.getPopularityBracket(skillBracket)));
                    }
                    bw.write(str.toString());
                    bw.newLine();
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMMRBracket(int bracket) {
        this.mmrBracket = bracket;
        for (Hero hero : heroes) {
            if (hero != null)
                hero.setBracket(bracket);
        }
    }

    private boolean loadMatchupsFromDotabuff() {
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

    private void loadMatchupsFromCache() {
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

    private void loadSynergiesFromCache() {
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

    private void writeMatchupsToCache() {
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

    private void addHero(int id, String name, String internalName) {
        Hero hero = new Hero(id, name, internalName);
        while (heroes.size() <= id) {
            heroes.add(null);
        }
        heroes.set(id, hero);
        heroMap.put(name, id);
    }

    public LinkedList<Hero> getAvailableHeroes() {
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

    public ArrayList<Hero> getAllHeroes() {
        return heroes;
    }

    public ArrayList<Hero> getStrengthHeroes() {
        ArrayList<Hero> strengthHeroes = new ArrayList<>();
        strengthHeroes.add(getHeroByName("Abaddon"));
        strengthHeroes.add(getHeroByName("Alchemist"));
        strengthHeroes.add(getHeroByName("Axe"));
        strengthHeroes.add(getHeroByName("Beastmaster"));
        strengthHeroes.add(getHeroByName("Brewmaster"));
        strengthHeroes.add(getHeroByName("Bristleback"));
        strengthHeroes.add(getHeroByName("Centaur Warrunner"));
        strengthHeroes.add(getHeroByName("Chaos Knight"));
        strengthHeroes.add(getHeroByName("Clockwerk"));
        strengthHeroes.add(getHeroByName("Doom"));
        strengthHeroes.add(getHeroByName("Dragon Knight"));
        strengthHeroes.add(getHeroByName("Earth Spirit"));
        strengthHeroes.add(getHeroByName("Earthshaker"));
        strengthHeroes.add(getHeroByName("Elder Titan"));
        strengthHeroes.add(getHeroByName("Huskar"));
        strengthHeroes.add(getHeroByName("Io"));
        strengthHeroes.add(getHeroByName("Kunkka"));
        strengthHeroes.add(getHeroByName("Legion Commander"));
        strengthHeroes.add(getHeroByName("Lifestealer"));
        strengthHeroes.add(getHeroByName("Lycan"));
        strengthHeroes.add(getHeroByName("Magnus"));
        strengthHeroes.add(getHeroByName("Night Stalker"));
        strengthHeroes.add(getHeroByName("Omniknight"));
        strengthHeroes.add(getHeroByName("Phoenix"));
        strengthHeroes.add(getHeroByName("Pudge"));
        strengthHeroes.add(getHeroByName("Sand King"));
        strengthHeroes.add(getHeroByName("Slardar"));
        strengthHeroes.add(getHeroByName("Spirit Breaker"));
        strengthHeroes.add(getHeroByName("Sven"));
        strengthHeroes.add(getHeroByName("Tidehunter"));
        strengthHeroes.add(getHeroByName("Timbersaw"));
        strengthHeroes.add(getHeroByName("Tiny"));
        strengthHeroes.add(getHeroByName("Treant Protector"));
        strengthHeroes.add(getHeroByName("Tusk"));
        strengthHeroes.add(getHeroByName("Underlord"));
        strengthHeroes.add(getHeroByName("Undying"));
        strengthHeroes.add(getHeroByName("Wraith King"));
        return strengthHeroes;
    }

    public ArrayList<Hero> getAgilityHeroes() {
        ArrayList<Hero> agilityHeroes = new ArrayList<>();
        agilityHeroes.add(getHeroByName("Anti-Mage"));
        agilityHeroes.add(getHeroByName("Arc Warden"));
        agilityHeroes.add(getHeroByName("Bloodseeker"));
        agilityHeroes.add(getHeroByName("Bounty Hunter"));
        agilityHeroes.add(getHeroByName("Broodmother"));
        agilityHeroes.add(getHeroByName("Clinkz"));
        agilityHeroes.add(getHeroByName("Drow Ranger"));
        agilityHeroes.add(getHeroByName("Ember Spirit"));
        agilityHeroes.add(getHeroByName("Faceless Void"));
        agilityHeroes.add(getHeroByName("Gyrocopter"));
        agilityHeroes.add(getHeroByName("Juggernaut"));
        agilityHeroes.add(getHeroByName("Lone Druid"));
        agilityHeroes.add(getHeroByName("Luna"));
        agilityHeroes.add(getHeroByName("Medusa"));
        agilityHeroes.add(getHeroByName("Meepo"));
        agilityHeroes.add(getHeroByName("Mirana"));
        agilityHeroes.add(getHeroByName("Monkey King"));
        agilityHeroes.add(getHeroByName("Morphling"));
        agilityHeroes.add(getHeroByName("Naga Siren"));
        agilityHeroes.add(getHeroByName("Nyx Assassin"));
        agilityHeroes.add(getHeroByName("Phantom Assassin"));
        agilityHeroes.add(getHeroByName("Phantom Lancer"));
        agilityHeroes.add(getHeroByName("Razor"));
        agilityHeroes.add(getHeroByName("Riki"));
        agilityHeroes.add(getHeroByName("Shadow Fiend"));
        agilityHeroes.add(getHeroByName("Slark"));
        agilityHeroes.add(getHeroByName("Sniper"));
        agilityHeroes.add(getHeroByName("Spectre"));
        agilityHeroes.add(getHeroByName("Templar Assassin"));
        agilityHeroes.add(getHeroByName("Terrorblade"));
        agilityHeroes.add(getHeroByName("Troll Warlord"));
        agilityHeroes.add(getHeroByName("Ursa"));
        agilityHeroes.add(getHeroByName("Vengeful Spirit"));
        agilityHeroes.add(getHeroByName("Venomancer"));
        agilityHeroes.add(getHeroByName("Viper"));
        agilityHeroes.add(getHeroByName("Weaver"));
        return agilityHeroes;
    }

    public ArrayList<Hero> getIntelligenceHeroes() {
        ArrayList<Hero> intelligenceHeroes = new ArrayList<>();
        intelligenceHeroes.add(getHeroByName("Ancient Apparition"));
        intelligenceHeroes.add(getHeroByName("Bane"));
        intelligenceHeroes.add(getHeroByName("Batrider"));
        intelligenceHeroes.add(getHeroByName("Chen"));
        intelligenceHeroes.add(getHeroByName("Crystal Maiden"));
        intelligenceHeroes.add(getHeroByName("Dark Seer"));
        intelligenceHeroes.add(getHeroByName("Dazzle"));
        intelligenceHeroes.add(getHeroByName("Death Prophet"));
        intelligenceHeroes.add(getHeroByName("Disruptor"));
        intelligenceHeroes.add(getHeroByName("Enchantress"));
        intelligenceHeroes.add(getHeroByName("Enigma"));
        intelligenceHeroes.add(getHeroByName("Invoker"));
        intelligenceHeroes.add(getHeroByName("Jakiro"));
        intelligenceHeroes.add(getHeroByName("Keeper of the Light"));
        intelligenceHeroes.add(getHeroByName("Leshrac"));
        intelligenceHeroes.add(getHeroByName("Lich"));
        intelligenceHeroes.add(getHeroByName("Lina"));
        intelligenceHeroes.add(getHeroByName("Lion"));
        intelligenceHeroes.add(getHeroByName("Nature's Prophet"));
        intelligenceHeroes.add(getHeroByName("Necrophos"));
        intelligenceHeroes.add(getHeroByName("Ogre Magi"));
        intelligenceHeroes.add(getHeroByName("Oracle"));
        intelligenceHeroes.add(getHeroByName("Outworld Devourer"));
        intelligenceHeroes.add(getHeroByName("Puck"));
        intelligenceHeroes.add(getHeroByName("Pugna"));
        intelligenceHeroes.add(getHeroByName("Queen of Pain"));
        intelligenceHeroes.add(getHeroByName("Rubick"));
        intelligenceHeroes.add(getHeroByName("Shadow Demon"));
        intelligenceHeroes.add(getHeroByName("Shadow Shaman"));
        intelligenceHeroes.add(getHeroByName("Silencer"));
        intelligenceHeroes.add(getHeroByName("Skywrath Mage"));
        intelligenceHeroes.add(getHeroByName("Storm Spirit"));
        intelligenceHeroes.add(getHeroByName("Techies"));
        intelligenceHeroes.add(getHeroByName("Tinker"));
        intelligenceHeroes.add(getHeroByName("Visage"));
        intelligenceHeroes.add(getHeroByName("Warlock"));
        intelligenceHeroes.add(getHeroByName("Windranger"));
        intelligenceHeroes.add(getHeroByName("Winter Wyvern"));
        intelligenceHeroes.add(getHeroByName("Witch Doctor"));
        intelligenceHeroes.add(getHeroByName("Zeus"));
        return intelligenceHeroes;
    }

}
