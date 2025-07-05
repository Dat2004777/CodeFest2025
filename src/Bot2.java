import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.Hero;

import java.io.IOException;


public class Bot2 {
    private static final String SERVER_URL = "https://cf25-server.jsclub.dev";
    private static final String GAME_ID = "187579";
    private static final String PLAYER_NAME = "NguyenDatJ";
    private static final String SECRET_KEY = "sk-JgopwyZLTWCaRMspEBg42w:8QdEy7f--WOBuGSOwAR2tUMNlh2-tIpaxEbo8uGvHljLfhRRdhF9nr4Ec268Sz9v_KoiACurtsBDasa_XahkYg";


    public static void main(String[] args) throws IOException {
        Hero hero = new Hero(GAME_ID, PLAYER_NAME, SECRET_KEY);
        Emitter.Listener onMapUpdate = new MapUpdateListener(hero);

        hero.setOnMapUpdate(onMapUpdate);
        hero.start(SERVER_URL);
    }
}