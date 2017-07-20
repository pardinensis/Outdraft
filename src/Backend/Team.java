package Backend;

import java.io.*;
import java.util.ArrayList;

public class Team {
    private ArrayList<Player> players;

    public Team() {
        players = new ArrayList<>();
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void loadFromCache() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Resources.FILENAME_CONFIG_TEAM));
            String line;
            while ((line = br.readLine()) != null) {
                long id = Long.valueOf(line);
                Player player = new Player(id);
                players.add(player);
            }
        }
        catch (IOException ioex) {
            ioex.printStackTrace();
            System.exit(1);
        }
    }

    private void writeToCache() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(Resources.FILENAME_CONFIG_TEAM));
            for (Player player : players) {
                bw.write("" + player.getId());
                bw.newLine();
            }
            bw.close();
        } catch (IOException ioex) {
            ioex.printStackTrace();
            System.exit(1);
        }
    }
}
