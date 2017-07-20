package Backend;

import java.io.*;
import java.util.ArrayList;

public class Team {
    private ArrayList<Player> players;

    public Team() {
        players = new ArrayList<>();
    }

    public ArrayList<Player> getActivePlayers() {
        ArrayList<Player> activePlayers = new ArrayList<>();
        for (Player player : players) {
            if (player.isPlaying()) {
                activePlayers.add(player);
            }
        }
        return activePlayers;
    }

    public ArrayList<Player> getAllPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
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

    public void writeToCache() {
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
