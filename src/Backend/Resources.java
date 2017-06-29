package Backend;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Resources {
    public static final String API_KEY = "A1D51A967209DA9D66F01E152098EAF3";
    public static final String API_GET_HEROES = "http://api.steampowered.com/IEconDOTA2_570/GetHeroes/v1/?";

    public static final String DOTABUFF_GET_HEROPOOL_1 = "https://www.dotabuff.com/players/";
    public static final String DOTABUFF_GET_HEROPOOL_2 = "/heroes?date=year&metric=played";

    public static final String FILENAME_CACHE_UPDATE_TIMESTAMP = "data/cache/lastupdate.txt";
    public static final String FILENAME_CACHE_HEROES = "data/cache/heroes.csv";
    public static final String FILENAME_CACHE_WINRATES_POPULARITY = "data/cache/winrates_popularity.csv";

    public static final String DIRNAME_CACHE_MATCHUPS = "data/cache/matchups/";
    public static final String DIRNAME_CACHE_PLAYERS = "data/cache/players/";
    public static final String DIRNAME_CACHE_SYNERGIES = "data/cache/synergies/";

    public static final String FILENAME_CONFIG_MMR_BRACKET = "data/config/mmr_bracket.txt";
    public static final String FILENAME_CONFIG_EXCLUDED_HEROES = "data/config/excluded_heroes.txt";
    public static final String FILENAME_CONFIG_POSITIONS = "data/config/positions.csv";
    public static final String FILENAME_CONFIG_TEAM = "data/config/team.txt";


    private static String get(String query) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(query);
        HttpResponse response = null;
        try {
            response = client.execute(request);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            return null;
        }
    }

    public static Map<String, Object> getObject(String query) {
        String response = get(query);
        if (response == null) {
            return null;
        }

        Gson gson = new Gson();
        try {
            Map<String, Object> result = gson.fromJson(response, new TypeToken<HashMap<String, Object>>(){}.getType());
            if (result.isEmpty()) {
                System.out.println("Empty Response - Maybe the Steam WebAPI is down");
            }
            return result;
        } catch (JsonSyntaxException e) {
//            e.printStackTrace();
            return null;
        }
    }
}
