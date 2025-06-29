import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.Hero;

import java.io.IOException;


public class Bot1 {
    private static final String SERVER_URL = "https://cf25-server.jsclub.dev";
    private static final String GAME_ID = "191005";
    private static final String PLAYER_NAME = "NguyenDatJ";
    private static final String SECRET_KEY = "sk-1SYeLIsLQemwnjbrb6UaDA:A9TgrTJxZ3A8fAcMn_ACri0zQlakF1p0Co7xBo0P8iYJQi6FTR5bmkYE1W8hFhLt0U4aai_RfYZZhywMLJ__EA";


    public static void main(String[] args) throws IOException {
        Hero hero = new Hero(GAME_ID, PLAYER_NAME, SECRET_KEY);
        Emitter.Listener onMapUpdate = new MapUpdateListener(hero);

        hero.setOnMapUpdate(onMapUpdate);
        hero.start(SERVER_URL);
    }
}