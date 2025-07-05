import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.Hero;

import java.io.IOException;


public class Main {
    private static final String SERVER_URL = "https://cf25-server.jsclub.dev";
    private static final String GAME_ID = "181274";
    private static final String PLAYER_NAME = "OVERCODE";
    private static final String SECRET_KEY = "sk-4ynpZklBRsydHRDzeqt7cg:PcnMFvBohG5NKs_MXtRq07Bi3PipeEBURBuOp-kraAbkgKoCgW1lpKnT6tQhN3msjZQfz4WqxTyU_qbwQ9y5rQ";


    public static void main(String[] args) throws IOException {
        Hero hero = new Hero(GAME_ID, PLAYER_NAME, SECRET_KEY);
        Emitter.Listener onMapUpdate = new MapUpdateListener(hero);

        hero.setOnMapUpdate(onMapUpdate);
        hero.start(SERVER_URL);
    }
}