package Backend;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class Player {
    private long id;
    private String name;
    private ArrayList<Integer> heroWins;
    private int totalWins;
    private String positions;


    public Player(long id) {
        this.id = id;
        name = "?";
        heroWins = new ArrayList<>();
        totalWins = 0;
        positions = "aaaaa";

        if (id > 0) {
            if (!loadFromCache()) {
                loadFromDotabuff();
                writeHeroPoolToCache();
            }
        }
    }

    public double getWinRateAddend(Hero hero) {
        double wins = heroWins.get(hero.getId());
        return - 0.1 * Math.exp(-120. / totalWins * wins);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setPositionsString(String positionsStr) {
        assert positionsStr.length() == 5;
        positions = positionsStr;
        writePositionsToCache();
    }

    public String getPositionsString() {
        return positions;
    }

    public double getPositionAddend(int position) {
        return Position.winRateAddend(positions.charAt(position));
    }

    private boolean loadFromCache() {
        heroWins = new ArrayList<>(Collections.nCopies(Heroes.getHeroesUpperBound(), 0));
        try {
            String filename = Resources.DIRNAME_CACHE_PLAYERS + id + "_heroes.csv";
            BufferedReader br = new BufferedReader(new FileReader(filename));
            br.readLine(); // drop header

            String line;
            while ((line = br.readLine()) != null) {
                String[] token = line.split(",");
                assert token.length == 2;
                int id = Integer.valueOf(token[0]);
                int wins = Integer.valueOf(token[1]);
                totalWins += wins;
                heroWins.set(id, wins);
            }

            filename = Resources.DIRNAME_CACHE_PLAYERS + id + ".csv";
            br = new BufferedReader(new FileReader(filename));
            br.readLine(); // drop header
            line = br.readLine();
            assert(line != null);
            String[] token = line.split(",");
            assert token.length == 2;
            name = token[0];
            assert token[1].length() == 5;
            positions = token[1];

            return true;
        } catch (IOException ioex) {
            return false;
        }
    }

    private void writeHeroPoolToCache() {
        try {
            String filename = Resources.DIRNAME_CACHE_PLAYERS + id + "_heroes.csv";
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            bw.write("Id,Wins");
            bw.newLine();
            for (int id = 0; id < heroWins.size(); ++id) {
                bw.write(id + "," + heroWins.get(id));
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writePositionsToCache() {
        try {
            String filename = Resources.DIRNAME_CACHE_PLAYERS + id + ".csv";
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            bw.write("Name,Positions");
            bw.newLine();
            bw.write(name + "," + getPositionsString());
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean loadFromDotabuff() {
        heroWins = new ArrayList<>(Collections.nCopies(Heroes.getHeroesUpperBound(), 0));
        try {
            String url = Resources.DOTABUFF_GET_HEROPOOL_1 + id + Resources.DOTABUFF_GET_HEROPOOL_2;
            Document doc = Jsoup.connect(url).get();

            Elements divs = doc.select("div.header-content-title");
            name = divs.get(0).child(0).ownText();

            Elements rows = doc.select("tr");
            for (Element row : rows) {
                Elements children = row.children();
                if (children.size() >= 6) {
                    String heroStr = row.child(0).attr("data-value");
                    if (!heroStr.equals("")) {
                        int matches = new Integer(row.child(2).attr("data-value"));
                        double winrate = new Double(row.child(3).attr("data-value"));
                        int wins = (int) Math.round(matches * (winrate / 100.));
                        Hero hero = Heroes.getHeroByName(heroStr);
                        heroWins.set(hero.getId(), wins);
                    }
                }
            }
            return true;
        } catch (IOException ioex) {
            System.err.println("cannot retrieve hero pool for player " + id);
            return false;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}

