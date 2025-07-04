import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.Hero;

import java.io.IOException;


public class Bot3 {
    private static final String SERVER_URL = "https://cf25-server.jsclub.dev";
    private static final String GAME_ID = "102027";
    private static final String PLAYER_NAME = "NguyenDatJ";
    private static final String SECRET_KEY = "sk-PhYI7GgDRherTCyTKuVmVg:1VT8azjKEj3iNHSxwyZN0Z58VGMJGFFNalWC025n4wN-5GMkpI8WboiuVLueleUzEjwNHfEyUN7QEmlQM6jNKg";


    public static void main(String[] args) throws IOException {
        Hero hero = new Hero(GAME_ID, PLAYER_NAME, SECRET_KEY);
        Emitter.Listener onMapUpdate = new MapUpdateListener(hero);

        hero.setOnMapUpdate(onMapUpdate);
        hero.start(SERVER_URL);
    }
}